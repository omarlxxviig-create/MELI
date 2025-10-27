package com.meli.inventory_service.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ServicePost representa una publicación de servicio de transporte.
 * Es la unidad reservable en el sistema (reemplaza Product/StoreInventory).
 */
@Entity
@Table(name = "service_post", indexes = {
        @Index(name = "idx_post_owner", columnList = "owner_id"),
        @Index(name = "idx_post_status", columnList = "status"),
        @Index(name = "idx_post_departure", columnList = "departure_date_time"),
        @Index(name = "idx_post_route", columnList = "origin, destination")
})
public class ServicePost {

    @Id
    @Column(name = "id", length = 36, updatable = false, nullable = false)
    private String id = UUID.randomUUID().toString();

    /**
     * Referencia al usuario propietario (conductor del veh\u00edculo)
     */
    @Column(name = "owner_id", nullable = false)
    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    @Column(name = "origin", nullable = false, length = 255)
    @NotBlank(message = "Origin is required")
    @Size(max = 255)
    private String origin;

    @Column(name = "destination", nullable = false, length = 255)
    @NotBlank(message = "Destination is required")
    @Size(max = 255)
    private String destination;

    @Column(name = "departure_date_time", nullable = false)
    @NotNull(message = "Departure date/time is required")
    @Future(message = "Departure must be in the future")
    private LocalDateTime departureDateTime;

    @Column(name = "seats_total", nullable = false)
    @Min(value = 1, message = "Must have at least 1 seat")
    @Max(value = 10, message = "Maximum 10 seats allowed")
    private Integer seatsTotal;

    @Column(name = "seats_available", nullable = false)
    @Min(value = 0, message = "Available seats cannot be negative")
    private Integer seatsAvailable;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be positive")
    private BigDecimal price;

    @Column(name = "description", length = 1000)
    @Size(max = 1000)
    private String description;

    /**
     * DRAFT, PUBLISHED, CANCELLED, COMPLETED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PostStatus status = PostStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Version para optimistic locking
     */
    @Version
    @Column(name = "version")
    private Integer version = 0;

    public enum PostStatus {
        DRAFT, // Borrador, no visible públicamente
        PUBLISHED, // Publicado y disponible para reservas
        CANCELLED, // Cancelado por el owner
        COMPLETED // Viaje completado
    }

    // Constructors
    public ServicePost() {
    }

    public ServicePost(Long ownerId, String origin, String destination,
            LocalDateTime departureDateTime, Integer seatsTotal, BigDecimal price) {
        this.ownerId = ownerId;
        this.origin = origin;
        this.destination = destination;
        this.departureDateTime = departureDateTime;
        this.seatsTotal = seatsTotal;
        this.seatsAvailable = seatsTotal; // Initially all seats available
        this.price = price;
    }

    // Business methods

    /**
     * Publica el post (lo hace visible para reservas)
     */
    public void publish() {
        if (this.status == PostStatus.DRAFT) {
            this.status = PostStatus.PUBLISHED;
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Only draft posts can be published");
        }
    }

    /**
     * Cancela el post
     */
    public void cancel() {
        if (this.status == PostStatus.PUBLISHED || this.status == PostStatus.DRAFT) {
            this.status = PostStatus.CANCELLED;
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Cannot cancel post in status: " + this.status);
        }
    }

    /**
     * Marca el post como completado
     */
    public void complete() {
        if (this.status == PostStatus.PUBLISHED) {
            this.status = PostStatus.COMPLETED;
            this.updatedAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Only published posts can be completed");
        }
    }

    /**
     * Reserva asientos (decrementa disponibles)
     */
    public void reserveSeats(int seats) {
        if (this.status != PostStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot reserve seats on non-published post");
        }
        if (seats <= 0) {
            throw new IllegalArgumentException("Seats to reserve must be positive");
        }
        if (this.seatsAvailable < seats) {
            throw new IllegalStateException(
                    String.format("Insufficient seats available. Requested: %d, Available: %d",
                            seats, this.seatsAvailable));
        }
        this.seatsAvailable -= seats;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Libera asientos (incrementa disponibles)
     */
    public void releaseSeats(int seats) {
        if (seats <= 0) {
            throw new IllegalArgumentException("Seats to release must be positive");
        }
        int newAvailable = this.seatsAvailable + seats;
        if (newAvailable > this.seatsTotal) {
            throw new IllegalStateException("Cannot release more seats than total capacity");
        }
        this.seatsAvailable = newAvailable;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica si hay suficientes asientos disponibles
     */
    public boolean hasAvailableSeats(int requestedSeats) {
        return this.seatsAvailable >= requestedSeats && this.status == PostStatus.PUBLISHED;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDateTime getDepartureDateTime() {
        return departureDateTime;
    }

    public void setDepartureDateTime(LocalDateTime departureDateTime) {
        this.departureDateTime = departureDateTime;
    }

    public Integer getSeatsTotal() {
        return seatsTotal;
    }

    public void setSeatsTotal(Integer seatsTotal) {
        this.seatsTotal = seatsTotal;
    }

    public Integer getSeatsAvailable() {
        return seatsAvailable;
    }

    public void setSeatsAvailable(Integer seatsAvailable) {
        this.seatsAvailable = seatsAvailable;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
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
