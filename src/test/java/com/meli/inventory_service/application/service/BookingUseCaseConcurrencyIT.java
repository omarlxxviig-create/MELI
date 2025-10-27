package com.meli.inventory_service.application.service;

import com.meli.inventory_service.domain.model.Reservation;
import com.meli.inventory_service.domain.model.ServicePost;
import com.meli.inventory_service.domain.ports.in.CreateReservationCommand;
import com.meli.inventory_service.domain.ports.in.CreateServicePostCommand;
import com.meli.inventory_service.domain.ports.out.OutboxPort;
import com.meli.inventory_service.domain.ports.out.ReservationPort;
import com.meli.inventory_service.domain.ports.out.ServicePostPort;
import com.meli.inventory_service.infrastructure.persistence.adapter.ReservationAdapter;
import com.meli.inventory_service.infrastructure.persistence.adapter.ServicePostAdapter;
import com.meli.inventory_service.infrastructure.persistence.spring.ReservationJpaRepository;
import com.meli.inventory_service.infrastructure.persistence.spring.ServicePostJpaRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Test de integración para verificar comportamiento bajo concurrencia.
 * Valida que el optimistic locking funciona correctamente y no hay overselling.
 */
@DataJpaTest
@ActiveProfiles("test")
class BookingUseCaseConcurrencyIT {

    @Autowired
    private ServicePostJpaRepository servicePostJpaRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @MockBean
    private OutboxPort outboxPort;

    private BookingUseCase bookingUseCase;

    @BeforeEach
    void setUp() {
        ServicePostPort servicePostPort = new ServicePostAdapter(servicePostJpaRepository);
        ReservationPort reservationPort = new ReservationAdapter(reservationJpaRepository);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

        bookingUseCase = new BookingUseCase(
                servicePostPort,
                reservationPort,
                outboxPort,
                meterRegistry);

        // Limpiar datos previos
        reservationJpaRepository.deleteAll();
        servicePostJpaRepository.deleteAll();
    }

    @Test
    void shouldHandleConcurrentReservationsCorrectly() throws InterruptedException {
        // Given: Crear un post con 5 asientos disponibles
        CreateServicePostCommand createPostCommand = new CreateServicePostCommand(
                1L,
                "Zipaquirá",
                "Bogotá",
                LocalDateTime.now().plusDays(1),
                5,
                BigDecimal.valueOf(15000));

        ServicePost post = bookingUseCase.createPost(createPostCommand);
        bookingUseCase.publishPost(post.getId());

        // When: 10 threads intentan reservar 1 asiento cada uno simultáneamente
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            final long userId = 100L + i;
            Future<Boolean> future = executorService.submit(() -> {
                try {
                    latch.countDown();
                    latch.await(); // Wait for all threads to be ready

                    CreateReservationCommand command = new CreateReservationCommand(
                            post.getId(),
                            userId,
                            1);

                    bookingUseCase.createReservation(command);
                    successCount.incrementAndGet();
                    return true;
                } catch (IllegalStateException e) {
                    // Expected: insufficient seats
                    failureCount.incrementAndGet();
                    return false;
                } catch (Exception e) {
                    // Unexpected error
                    e.printStackTrace();
                    return false;
                }
            });
            futures.add(future);
        }

        // Wait for all threads to complete
        executorService.shutdown();
        boolean finished = executorService.awaitTermination(30, TimeUnit.SECONDS);

        // Then: Exactamente 5 reservas exitosas (igual a asientos disponibles)
        assertThat(finished).isTrue();
        assertThat(successCount.get()).isEqualTo(5);
        assertThat(failureCount.get()).isEqualTo(5);

        // Verificar que el post tiene 0 asientos disponibles
        ServicePost updatedPost = bookingUseCase.getPostById(post.getId()).orElseThrow();
        assertThat(updatedPost.getSeatsAvailable()).isEqualTo(0);

        // Verificar que hay exactamente 5 reservas en la BD
        List<Reservation> reservations = bookingUseCase.getPostReservations(post.getId());
        assertThat(reservations).hasSize(5);
    }

    @Test
    void shouldHandleConcurrentReservationsWithDifferentSizes() throws InterruptedException {
        // Given: Post con 10 asientos
        CreateServicePostCommand createPostCommand = new CreateServicePostCommand(
                1L,
                "Bogotá",
                "Medellín",
                LocalDateTime.now().plusDays(1),
                10,
                BigDecimal.valueOf(50000));

        ServicePost post = bookingUseCase.createPost(createPostCommand);
        bookingUseCase.publishPost(post.getId());

        // When: Varios threads reservan diferentes cantidades
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);

        AtomicInteger totalSeatsReserved = new AtomicInteger(0);
        List<Integer> reservationSizes = List.of(3, 2, 4, 2, 3); // Total = 14 (más que disponible)

        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < reservationSizes.size(); i++) {
            final long userId = 200L + i;
            final int seats = reservationSizes.get(i);

            Future<Boolean> future = executorService.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();

                    CreateReservationCommand command = new CreateReservationCommand(
                            post.getId(),
                            userId,
                            seats);

                    bookingUseCase.createReservation(command);
                    totalSeatsReserved.addAndGet(seats);
                    return true;
                } catch (IllegalStateException e) {
                    // Expected: insufficient seats
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            });
            futures.add(future);
        }

        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        // Then: Total de asientos reservados debe ser <= 10
        assertThat(totalSeatsReserved.get()).isLessThanOrEqualTo(10);

        ServicePost updatedPost = bookingUseCase.getPostById(post.getId()).orElseThrow();
        assertThat(updatedPost.getSeatsAvailable()).isGreaterThanOrEqualTo(0);
        assertThat(updatedPost.getSeatsAvailable()).isEqualTo(10 - totalSeatsReserved.get());
    }

    @Test
    void shouldHandleConcurrentCancellations() throws InterruptedException {
        // Given: Crear post y reservas
        CreateServicePostCommand createPostCommand = new CreateServicePostCommand(
                1L,
                "Chía",
                "Bogotá",
                LocalDateTime.now().plusDays(1),
                5,
                BigDecimal.valueOf(12000));

        ServicePost post = bookingUseCase.createPost(createPostCommand);
        bookingUseCase.publishPost(post.getId());

        // Crear 3 reservas
        List<String> reservationIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            CreateReservationCommand command = new CreateReservationCommand(
                    post.getId(),
                    300L + i,
                    1);
            Reservation res = bookingUseCase.createReservation(command);
            reservationIds.add(res.getId());
        }

        // When: Cancelar las 3 reservas concurrentemente
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        AtomicInteger successfulCancellations = new AtomicInteger(0);

        for (String resId : reservationIds) {
            executorService.submit(() -> {
                try {
                    bookingUseCase.cancelReservation(resId);
                    successfulCancellations.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // Then: Todas las cancelaciones exitosas, asientos liberados
        assertThat(successfulCancellations.get()).isEqualTo(3);

        ServicePost updatedPost = bookingUseCase.getPostById(post.getId()).orElseThrow();
        assertThat(updatedPost.getSeatsAvailable()).isEqualTo(5); // All seats released
    }
}
