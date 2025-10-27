package com.meli.inventory_service.infrastructure.rest;

import com.meli.inventory_service.application.service.BookingUseCase;
import com.meli.inventory_service.domain.model.Reservation;
import com.meli.inventory_service.domain.ports.in.CreateReservationCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de reservas
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Reservations", description = "Gestión de reservas de asientos")
public class ReservationController {

    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    private final BookingUseCase bookingUseCase;

    public ReservationController(BookingUseCase bookingUseCase) {
        this.bookingUseCase = bookingUseCase;
    }

    @PostMapping("/posts/{postId}/reserve")
    @Operation(summary = "Crear una reserva de asientos")
    public ResponseEntity<ReservationResponse> createReservation(
            @PathVariable String postId,
            @Valid @RequestBody CreateReservationRequest request) {

        logger.info("POST /api/v1/posts/{}/reserve - Creating reservation", postId);

        CreateReservationCommand command = new CreateReservationCommand(
                postId,
                request.getUserId(),
                request.getSeats());

        try {
            Reservation reservation = bookingUseCase.createReservation(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(reservation));
        } catch (IllegalStateException e) {
            logger.warn("Reservation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null); // Se podría retornar un error DTO estructurado
        }
    }

    @PutMapping("/reservations/{id}/cancel")
    @Operation(summary = "Cancelar una reserva")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable String id) {
        logger.info("PUT /api/v1/reservations/{}/cancel - Cancelling reservation", id);

        try {
            Reservation reservation = bookingUseCase.cancelReservation(id);
            return ResponseEntity.ok(toResponse(reservation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/reservations/{id}/confirm")
    @Operation(summary = "Confirmar una reserva")
    public ResponseEntity<ReservationResponse> confirmReservation(@PathVariable String id) {
        logger.info("PUT /api/v1/reservations/{}/confirm - Confirming reservation", id);

        try {
            Reservation reservation = bookingUseCase.confirmReservation(id);
            return ResponseEntity.ok(toResponse(reservation));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/reservations/{id}")
    @Operation(summary = "Obtener detalle de una reserva")
    public ResponseEntity<ReservationResponse> getReservation(@PathVariable String id) {
        logger.info("GET /api/v1/reservations/{} - Retrieving reservation", id);

        return bookingUseCase.getReservationById(id)
                .map(reservation -> ResponseEntity.ok(toResponse(reservation)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{userId}/reservations")
    @Operation(summary = "Listar reservas de un usuario")
    public ResponseEntity<List<ReservationResponse>> getUserReservations(@PathVariable Long userId) {
        logger.info("GET /api/v1/users/{}/reservations - Listing user reservations", userId);

        List<Reservation> reservations = bookingUseCase.getUserReservations(userId);
        List<ReservationResponse> response = reservations.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/posts/{postId}/reservations")
    @Operation(summary = "Listar reservas de una publicación")
    public ResponseEntity<List<ReservationResponse>> getPostReservations(@PathVariable String postId) {
        logger.info("GET /api/v1/posts/{}/reservations - Listing post reservations", postId);

        List<Reservation> reservations = bookingUseCase.getPostReservations(postId);
        List<ReservationResponse> response = reservations.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    private ReservationResponse toResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setPostId(reservation.getPostId());
        response.setUserId(reservation.getUserId());
        response.setSeats(reservation.getSeats());
        response.setStatus(reservation.getStatus().name());
        response.setExpiresAt(reservation.getExpiresAt());
        response.setCreatedAt(reservation.getCreatedAt());
        response.setUpdatedAt(reservation.getUpdatedAt());
        return response;
    }
}
