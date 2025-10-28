package com.meli.inventory_service.application.service;

import com.meli.inventory_service.domain.model.OutboxMessage;
import com.meli.inventory_service.domain.model.Reservation;
import com.meli.inventory_service.domain.model.Reservation.ReservationStatus;
import com.meli.inventory_service.domain.model.ServicePost;
import com.meli.inventory_service.domain.model.ServicePost.PostStatus;
import com.meli.inventory_service.domain.ports.in.BookingPort;
import com.meli.inventory_service.domain.ports.in.CreateReservationCommand;
import com.meli.inventory_service.domain.ports.in.CreateServicePostCommand;
import com.meli.inventory_service.domain.ports.out.OutboxPort;
import com.meli.inventory_service.domain.ports.out.ReservationPort;
import com.meli.inventory_service.domain.ports.out.ServicePostPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Caso de uso principal para gestión de reservas de transporte
 */
@Service
public class BookingUseCase implements BookingPort {

        private static final Logger logger = LoggerFactory.getLogger(BookingUseCase.class);
        private static final int DEFAULT_RESERVATION_TTL_MINUTES = 15;

        private final ServicePostPort servicePostPort;
        private final ReservationPort reservationPort;
        private final OutboxPort outboxPort;

        private final Counter reservationsCreatedCounter;
        private final Counter reservationConflictsCounter;
        private final Counter outboxPendingCounter;
        private final Timer reservationDurationTimer;

        public BookingUseCase(ServicePostPort servicePostPort,
                        ReservationPort reservationPort,
                        OutboxPort outboxPort,
                        MeterRegistry meterRegistry) {
                this.servicePostPort = servicePostPort;
                this.reservationPort = reservationPort;
                this.outboxPort = outboxPort;

                // Métricas
                this.reservationsCreatedCounter = Counter.builder("reservations_created_total")
                                .description("Total number of reservations created")
                                .register(meterRegistry);

                this.reservationConflictsCounter = Counter.builder("reservation_conflicts_total")
                                .description("Total number of reservation conflicts (insufficient seats)")
                                .register(meterRegistry);

                this.outboxPendingCounter = Counter.builder("outbox_pending_count")
                                .description("Number of pending outbox messages")
                                .register(meterRegistry);

                this.reservationDurationTimer = Timer.builder("reservation_duration_seconds")
                                .description("Duration of reservation operations")
                                .register(meterRegistry);
        }

        @Override
        @Transactional
        public ServicePost createPost(@Valid CreateServicePostCommand command) {
                logger.info("Creating service post: {} -> {} at {}",
                                command.getOrigin(), command.getDestination(), command.getDepartureDateTime());

                ServicePost post = new ServicePost(
                                command.getOwnerId(),
                                command.getOrigin(),
                                command.getDestination(),
                                command.getDepartureDateTime(),
                                command.getSeatsTotal(),
                                command.getPrice());
                post.setDescription(command.getDescription());
                post.setStatus(PostStatus.DRAFT); // Asegurar que inicie como DRAFT

                ServicePost savedPost = servicePostPort.save(post);

                // Publicar evento de creación
                createOutboxEvent("ServicePost", savedPost.getId(), "POST_CREATED",
                                String.format("{\"postId\":\"%s\",\"ownerId\":%d,\"route\":\"%s -> %s\"}",
                                                savedPost.getId(), savedPost.getOwnerId(),
                                                savedPost.getOrigin(), savedPost.getDestination()));

                logger.info("Service post created successfully: {}", savedPost.getId());
                return savedPost;
        }

        @Override
        @Transactional
        public ServicePost publishPost(String postId) {
                logger.info("Publishing service post: {}", postId);

                ServicePost post = servicePostPort.findById(postId)
                                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

                post.publish();
                ServicePost published = servicePostPort.save(post);

                createOutboxEvent("ServicePost", postId, "POST_PUBLISHED",
                                String.format("{\"postId\":\"%s\",\"status\":\"PUBLISHED\"}", postId));

                logger.info("Service post published: {}", postId);
                return published;
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<ServicePost> getPostById(String postId) {
                return servicePostPort.findById(postId);
        }

        @Override
        @Transactional(readOnly = true)
        public List<ServicePost> listAvailablePosts() {
                return servicePostPort.findByStatus(PostStatus.PUBLISHED);
        }

        @Override
        @Transactional(readOnly = true)
        public List<ServicePost> searchPosts(String origin, String destination,
                        LocalDateTime fromDate, LocalDateTime toDate) {
                return servicePostPort.findByRouteAndDateRange(origin, destination, fromDate, toDate);
        }

        @Override
        @Transactional
        @Retryable(retryFor = {
                        ObjectOptimisticLockingFailureException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2.0))
        public Reservation createReservation(@Valid CreateReservationCommand command) {
                return reservationDurationTimer.record(() -> {
                        logger.info("Creating reservation for post: {}, user: {}, seats: {}",
                                        command.getPostId(), command.getUserId(), command.getSeats());

                        // Lock post para validación
                        ServicePost post = servicePostPort.findByIdForUpdate(command.getPostId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Post not found: " + command.getPostId()));

                        // Solo validar disponibilidad (no descontar)
                        post.validateSeatsAvailability(command.getSeats());

                        // Crear reserva en estado PENDING
                        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(DEFAULT_RESERVATION_TTL_MINUTES);
                        Reservation reservation = new Reservation(
                                        command.getPostId(),
                                        command.getUserId(),
                                        command.getSeats(),
                                        expiresAt);

                        Reservation savedReservation = reservationPort.save(reservation);

                        // Crear evento outbox
                        createOutboxEvent("Reservation", savedReservation.getId(), "RESERVATION_CREATED",
                                        String.format("{\"reservationId\":\"%s\",\"postId\":\"%s\",\"userId\":%d,\"seats\":%d}",
                                                        savedReservation.getId(), command.getPostId(),
                                                        command.getUserId(), command.getSeats()));

                        reservationsCreatedCounter.increment();
                        return savedReservation;
                });
        }

        @Override
        @Transactional
        public Reservation cancelReservation(String reservationId) {
                logger.info("Cancelling reservation: {}", reservationId);

                Reservation reservation = reservationPort.findByIdForUpdate(reservationId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Reservation not found: " + reservationId));

                // Liberar asientos del post
                ServicePost post = servicePostPort.findByIdForUpdate(reservation.getPostId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Post not found: " + reservation.getPostId()));

                post.releaseSeats(reservation.getSeats());
                servicePostPort.save(post);

                // Cancelar reserva
                reservation.cancel();
                Reservation cancelled = reservationPort.save(reservation);

                createOutboxEvent("Reservation", reservationId, "RESERVATION_CANCELLED",
                                String.format("{\"reservationId\":\"%s\",\"postId\":\"%s\",\"seats\":%d}",
                                                reservationId, reservation.getPostId(), reservation.getSeats()));

                logger.info("Reservation cancelled: {}", reservationId);
                return cancelled;
        }

        @Override
        @Transactional
        public Reservation confirmReservation(String reservationId) {
                logger.info("Confirming reservation: {}", reservationId);

                Reservation reservation = reservationPort.findByIdForUpdate(reservationId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Reservation not found: " + reservationId));

                // Confirmar reserva (descuenta asientos)
                ServicePost post = servicePostPort.findByIdForUpdate(reservation.getPostId())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Post not found: " + reservation.getPostId()));

                post.confirmSeats(reservation.getSeats());
                servicePostPort.save(post);

                reservation.confirm();
                return reservationPort.save(reservation);
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<Reservation> getReservationById(String reservationId) {
                return reservationPort.findById(reservationId);
        }

        @Override
        @Transactional(readOnly = true)
        public List<Reservation> getUserReservations(Long userId) {
                return reservationPort.findByUserId(userId);
        }

        @Override
        @Transactional(readOnly = true)
        public List<Reservation> getPostReservations(String postId) {
                return reservationPort.findByPostId(postId);
        }

        @Override
        @Transactional
        public int processExpiredReservations() {
                logger.info("Processing expired reservations...");

                List<Reservation> expiredReservations = reservationPort
                                .findExpiredReservations(LocalDateTime.now());

                int count = 0;
                for (Reservation reservation : expiredReservations) {
                        if (reservation.getStatus() == ReservationStatus.PENDING) {
                                try {
                                        // Liberar asientos
                                        ServicePost post = servicePostPort.findByIdForUpdate(reservation.getPostId())
                                                        .orElse(null);
                                        if (post != null) {
                                                post.releaseSeats(reservation.getSeats());
                                                servicePostPort.save(post);
                                        }

                                        // Marcar como expirada
                                        reservation.expire();
                                        reservationPort.save(reservation);

                                        createOutboxEvent("Reservation", reservation.getId(), "RESERVATION_EXPIRED",
                                                        String.format("{\"reservationId\":\"%s\"}",
                                                                        reservation.getId()));

                                        count++;
                                } catch (Exception e) {
                                        logger.error("Error processing expired reservation: {}", reservation.getId(),
                                                        e);
                                }
                        }
                }

                logger.info("Processed {} expired reservations", count);
                return count;
        }

        private void createOutboxEvent(String aggregateType, String aggregateId,
                        String eventType, String payload) {
                OutboxMessage message = new OutboxMessage();
                message.setAggregateType(aggregateType);
                message.setAggregateId(aggregateId);
                message.setTopic(eventType); // Using topic field instead of eventType
                message.setPayload(payload);

                outboxPort.save(message);
                outboxPendingCounter.increment();

                logger.debug("Outbox event created: {} - {}", aggregateType, eventType);
        }
}
