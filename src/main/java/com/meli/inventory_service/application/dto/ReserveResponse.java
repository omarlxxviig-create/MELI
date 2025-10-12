package com.meli.inventory_service.application.dto;

import java.time.Instant;

/**
 * DTO para respuestas de reserva de inventario.
 * Pertenece a la capa de aplicación para desacoplar
 * la API REST del dominio.
 */
public class ReserveResponse {
    private String reservationId;
    private String status;
    private Instant expiresAt;

    public ReserveResponse() {
    }

    public ReserveResponse(String reservationId, String status, Instant expiresAt) {
        this.reservationId = reservationId;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
