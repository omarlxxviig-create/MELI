package com.meli.inventory_service.domain.ports.out;

import com.meli.inventory_service.domain.model.Reservation;
import com.meli.inventory_service.domain.model.Reservation.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de Reservation
 */
public interface ReservationPort {

    /**
     * Guarda una reserva (create o update)
     */
    Reservation save(Reservation reservation);

    /**
     * Busca por ID
     */
    Optional<Reservation> findById(String id);

    /**
     * Busca por ID con lock para actualización
     */
    Optional<Reservation> findByIdForUpdate(String id);

    /**
     * Lista todas las reservas
     */
    List<Reservation> findAll();

    /**
     * Busca reservas por usuario
     */
    List<Reservation> findByUserId(Long userId);

    /**
     * Busca reservas por post
     */
    List<Reservation> findByPostId(String postId);

    /**
     * Busca reservas por status
     */
    List<Reservation> findByStatus(ReservationStatus status);

    /**
     * Busca reservas expiradas
     */
    List<Reservation> findExpiredReservations(LocalDateTime now);

    /**
     * Elimina una reserva
     */
    void delete(String id);

    /**
     * Verifica si existe una reserva
     */
    boolean existsById(String id);
}
