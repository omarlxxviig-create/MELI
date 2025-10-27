package com.meli.inventory_service.infrastructure.persistence.spring;

import com.meli.inventory_service.domain.model.Reservation;
import com.meli.inventory_service.domain.model.Reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para Reservation
 */
@Repository
public interface ReservationJpaRepository extends JpaRepository<Reservation, String> {

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
     * Busca reservas expiradas pendientes
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' AND r.expiresAt < :now")
    List<Reservation> findExpiredReservations(@Param("now") LocalDateTime now);

    /**
     * Busca por ID con lock para actualización
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") String id);
}
