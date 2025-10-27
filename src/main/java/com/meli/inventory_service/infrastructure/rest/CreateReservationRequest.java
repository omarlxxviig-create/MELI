package com.meli.inventory_service.infrastructure.rest;

import jakarta.validation.constraints.*;

/**
 * DTO de request para crear reserva
 */
public class CreateReservationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Seats is required")
    @Min(value = 1, message = "Must reserve at least 1 seat")
    @Max(value = 10, message = "Maximum 10 seats per reservation")
    private Integer seats;

    // Getters and Setters
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
}
