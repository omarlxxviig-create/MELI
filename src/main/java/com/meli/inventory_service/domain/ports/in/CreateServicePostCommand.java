package com.meli.inventory_service.domain.ports.in;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Command para crear una nueva publicación de servicio
 */
public class CreateServicePostCommand {

    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    @NotBlank(message = "Origin is required")
    @Size(max = 255)
    private String origin;

    @NotBlank(message = "Destination is required")
    @Size(max = 255)
    private String destination;

    @NotNull(message = "Departure date/time is required")
    @Future(message = "Departure must be in the future")
    private LocalDateTime departureDateTime;

    @NotNull(message = "Seats total is required")
    @Min(value = 1, message = "Must have at least 1 seat")
    @Max(value = 10, message = "Maximum 10 seats allowed")
    private Integer seatsTotal;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be positive")
    private BigDecimal price;

    @Size(max = 1000)
    private String description;

    // Constructors
    public CreateServicePostCommand() {
    }

    public CreateServicePostCommand(Long ownerId, String origin, String destination,
            LocalDateTime departureDateTime, Integer seatsTotal, BigDecimal price) {
        this.ownerId = ownerId;
        this.origin = origin;
        this.destination = destination;
        this.departureDateTime = departureDateTime;
        this.seatsTotal = seatsTotal;
        this.price = price;
    }

    // Getters and Setters
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
}
