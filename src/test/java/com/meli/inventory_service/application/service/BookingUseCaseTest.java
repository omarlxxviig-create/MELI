package com.meli.inventory_service.application.service;

import com.meli.inventory_service.domain.model.Reservation;
import com.meli.inventory_service.domain.model.ServicePost;
import com.meli.inventory_service.domain.ports.in.CreateReservationCommand;
import com.meli.inventory_service.domain.ports.in.CreateServicePostCommand;
import com.meli.inventory_service.domain.ports.out.OutboxPort;
import com.meli.inventory_service.domain.ports.out.ReservationPort;
import com.meli.inventory_service.domain.ports.out.ServicePostPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para BookingUseCase
 */
@ExtendWith(MockitoExtension.class)
class BookingUseCaseTest {

    @Mock
    private ServicePostPort servicePostPort;

    @Mock
    private ReservationPort reservationPort;

    @Mock
    private OutboxPort outboxPort;

    private BookingUseCase bookingUseCase;

    @BeforeEach
    void setUp() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        bookingUseCase = new BookingUseCase(
                servicePostPort,
                reservationPort,
                outboxPort,
                meterRegistry);
    }

    @Test
    void shouldCreateServicePost() {
        // Given
        CreateServicePostCommand command = new CreateServicePostCommand(
                1L,
                "Zipaquirá",
                "Bogotá",
                LocalDateTime.now().plusDays(1),
                4,
                BigDecimal.valueOf(15000));

        ServicePost expectedPost = new ServicePost(
                command.getOwnerId(),
                command.getOrigin(),
                command.getDestination(),
                command.getDepartureDateTime(),
                command.getSeatsTotal(),
                command.getPrice());
        expectedPost.setId("post-001");

        when(servicePostPort.save(any(ServicePost.class))).thenReturn(expectedPost);

        // When
        ServicePost result = bookingUseCase.createPost(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("post-001");
        assertThat(result.getOrigin()).isEqualTo("Zipaquirá");
        assertThat(result.getDestination()).isEqualTo("Bogotá");
        assertThat(result.getSeatsTotal()).isEqualTo(4);
        assertThat(result.getSeatsAvailable()).isEqualTo(4);

        verify(servicePostPort).save(any(ServicePost.class));
        verify(outboxPort).save(any());
    }

    @Test
    void shouldPublishPost() {
        // Given
        String postId = "post-001";
        ServicePost post = new ServicePost(
                1L,
                "Zipaquirá",
                "Bogotá",
                LocalDateTime.now().plusDays(1),
                4,
                BigDecimal.valueOf(15000));
        post.setId(postId);

        when(servicePostPort.findById(postId)).thenReturn(Optional.of(post));
        when(servicePostPort.save(any(ServicePost.class))).thenReturn(post);

        // When
        ServicePost result = bookingUseCase.publishPost(postId);

        // Then
        assertThat(result.getStatus()).isEqualTo(ServicePost.PostStatus.PUBLISHED);
        verify(servicePostPort).save(any(ServicePost.class));
        verify(outboxPort).save(any());
    }

    @Test
    void shouldCreateReservationSuccessfully() {
        // Given
        String postId = "post-001";
        CreateReservationCommand command = new CreateReservationCommand(postId, 10L, 2);

        ServicePost post = new ServicePost(
                1L,
                "Zipaquirá",
                "Bogotá",
                LocalDateTime.now().plusDays(1),
                4,
                BigDecimal.valueOf(15000));
        post.setId(postId);
        post.setStatus(ServicePost.PostStatus.PUBLISHED);
        post.setSeatsAvailable(4);

        Reservation expectedReservation = new Reservation(postId, 10L, 2, LocalDateTime.now().plusMinutes(15));
        expectedReservation.setId("res-001");

        when(servicePostPort.findByIdForUpdate(postId)).thenReturn(Optional.of(post));
        when(servicePostPort.save(any(ServicePost.class))).thenReturn(post);
        when(reservationPort.save(any(Reservation.class))).thenReturn(expectedReservation);

        // When
        Reservation result = bookingUseCase.createReservation(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("res-001");
        assertThat(result.getPostId()).isEqualTo(postId);
        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getSeats()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(Reservation.ReservationStatus.PENDING);

        verify(servicePostPort).findByIdForUpdate(postId);
        verify(servicePostPort).save(any(ServicePost.class));
        verify(reservationPort).save(any(Reservation.class));
        verify(outboxPort).save(any());
    }

    @Test
    void shouldFailReservationWhenInsufficientSeats() {
        // Given
        String postId = "post-001";
        CreateReservationCommand command = new CreateReservationCommand(postId, 10L, 5);

        ServicePost post = new ServicePost(
                1L,
                "Zipaquirá",
                "Bogotá",
                LocalDateTime.now().plusDays(1),
                4,
                BigDecimal.valueOf(15000));
        post.setId(postId);
        post.setStatus(ServicePost.PostStatus.PUBLISHED);
        post.setSeatsAvailable(2); // Only 2 seats available

        when(servicePostPort.findByIdForUpdate(postId)).thenReturn(Optional.of(post));

        // When / Then
        assertThatThrownBy(() -> bookingUseCase.createReservation(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient seats");

        verify(servicePostPort).findByIdForUpdate(postId);
        verify(servicePostPort, never()).save(any());
        verify(reservationPort, never()).save(any());
    }

    @Test
    void shouldCancelReservationAndReleaseSeats() {
        // Given
        String reservationId = "res-001";
        String postId = "post-001";

        Reservation reservation = new Reservation(postId, 10L, 2, LocalDateTime.now().plusMinutes(15));
        reservation.setId(reservationId);

        ServicePost post = new ServicePost(
                1L,
                "Zipaquirá",
                "Bogotá",
                LocalDateTime.now().plusDays(1),
                4,
                BigDecimal.valueOf(15000));
        post.setId(postId);
        post.setSeatsAvailable(2); // 2 available before cancellation

        when(reservationPort.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));
        when(servicePostPort.findByIdForUpdate(postId)).thenReturn(Optional.of(post));
        when(servicePostPort.save(any(ServicePost.class))).thenReturn(post);
        when(reservationPort.save(any(Reservation.class))).thenReturn(reservation);

        // When
        Reservation result = bookingUseCase.cancelReservation(reservationId);

        // Then
        assertThat(result.getStatus()).isEqualTo(Reservation.ReservationStatus.CANCELLED);

        verify(reservationPort).findByIdForUpdate(reservationId);
        verify(servicePostPort).findByIdForUpdate(postId);
        verify(servicePostPort).save(argThat(p -> p.getSeatsAvailable() == 4)); // 2 + 2 released
        verify(reservationPort).save(any(Reservation.class));
        verify(outboxPort).save(any());
    }

    @Test
    void shouldConfirmReservation() {
        // Given
        String reservationId = "res-001";
        Reservation reservation = new Reservation("post-001", 10L, 2, LocalDateTime.now().plusMinutes(15));
        reservation.setId(reservationId);

        when(reservationPort.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationPort.save(any(Reservation.class))).thenReturn(reservation);

        // When
        Reservation result = bookingUseCase.confirmReservation(reservationId);

        // Then
        assertThat(result.getStatus()).isEqualTo(Reservation.ReservationStatus.CONFIRMED);
        verify(reservationPort).save(any(Reservation.class));
        verify(outboxPort).save(any());
    }

    @Test
    void shouldThrowExceptionWhenPostNotFound() {
        // Given
        String postId = "non-existent";
        when(servicePostPort.findById(postId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> bookingUseCase.publishPost(postId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    void shouldThrowExceptionWhenReservationNotFound() {
        // Given
        String reservationId = "non-existent";
        when(reservationPort.findById(reservationId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> bookingUseCase.confirmReservation(reservationId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reservation not found");
    }
}
