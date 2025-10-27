package com.meli.inventory_service.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/**
 * Reservation representa una reserva de asientos en un ServicePost.
 * Mantiene el estado de la reserva y puede expirar automáticamente.
 */
@Entity
@Table(name = "reservation", indexes = {
        @Index(name = "idx_reservation_post_id", columnList = "post_id"),
        @Index(name = "idx_reservation_user_id", columnList = "user_id"),
        @Index(name = "idx_reservation_status", columnList = "status"),
        @Index(name = "idx_reservation_expires_at", columnList = "expires_at")
})
public class Reservation {

    @Id
    @Column(name = "id", length = 36, updatable = false, nullable = false)
    private String id = UUID.randomUUID().toString();

    /**
     * Referencia al ServicePost (viaje)
     */
    @Column(name = "post_id", nullable = false, length = 36)
    @NotBlank(message = "Post ID is required")
    private String postId;

    /**
     * Usuario que realiza la reserva (pasajero)
     */
    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID is required")
    private Long userId;

    /**
     * Número de asientos reservados
     */
    @Column(name = "seats", nullable = false)
    @Min(value = 1, message = "Must reserve at least 1 seat")
    @Max(value = 10, message = "Maximum 10 seats per reservation")
    private Integer seats;

    /**
     * Estado de la reserva
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.PENDING;

    /**
     * Fecha y hora de expiración de la reserva
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Detalles opcionales de asignación de asientos (JSON)
     */
    @Column(name = "seat_allocation_details", length = 1000)
    private String seatAllocationDetails;

    @Version
    @Column(name = "version")
    private Integer version = 0;

    /**
     * Estados posibles de una reserva
     */
    public enum ReservationStatus {
        PENDING, // Reserva creada, esperando confirmación
        CONFIRMED, // Confirmada por el pasajero/sistema
        CANCELLED, // Cancelada por el usuario o admin
        EXPIRED // Expiró automáticamente
    }

    // Constructors
    public Reservation() {
    }

    public Reservation(String postId, Long userId, Integer seats, LocalDateTime expiresAt) {
        this.postId = postId;
        this.userId = userId;
        this.seats = seats;
        this.expiresAt = expiresAt;
    }

    // Business methods

    /**
     * Confirma la reserva
     */
    public void confirm() {
        if (this.status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Only pending reservations can be confirmed");
        }
        if (isExpired()) {
            throw new IllegalStateException("Cannot confirm expired reservation");
        }
        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cancela la reserva
     */
    public void cancel() {
        if (this.status == ReservationStatus.EXPIRED) {
            throw new IllegalStateException("Cannot cancel expired reservation");
        }
        if (this.status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation already cancelled");
        }
        this.status = ReservationStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marca como expirada
     */
    public void expire() {
        if (this.status == ReservationStatus.PENDING) {
            this.status = ReservationStatus.EXPIRED;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Verifica si la reserva ha expirado
     */
    public boolean isExpired() {
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Verifica si la reserva está activa
     */
    public boolean isActive() {
        return (this.status == ReservationStatus.PENDING || this.status == ReservationStatus.CONFIRMED)
                && !isExpired();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSeatAllocationDetails() {
        return seatAllocationDetails;
    }

    public void setSeatAllocationDetails(String seatAllocationDetails) {
        this.seatAllocationDetails = seatAllocationDetails;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
