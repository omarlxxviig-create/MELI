package com.meli.inventory_service.domain.ports.in;

import jakarta.validation.constraints.*;

/**
 * Command para crear una reserva de asientos
 */
public class CreateReservationCommand {

    @NotBlank(message = "Post ID is required")
    private String postId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Seats is required")
    @Min(value = 1, message = "Must reserve at least 1 seat")
    @Max(value = 10, message = "Maximum 10 seats per reservation")
    private Integer seats;

    // Constructors
    public CreateReservationCommand() {
    }

    public CreateReservationCommand(String postId, Long userId, Integer seats) {
        this.postId = postId;
        this.userId = userId;
        this.seats = seats;
    }

    // Getters and Setters
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
}
