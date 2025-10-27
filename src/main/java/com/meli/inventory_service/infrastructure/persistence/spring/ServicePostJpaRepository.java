package com.meli.inventory_service.infrastructure.persistence.spring;

import com.meli.inventory_service.domain.model.ServicePost;
import com.meli.inventory_service.domain.model.ServicePost.PostStatus;
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
 * Repositorio JPA para ServicePost
 */
@Repository
public interface ServicePostJpaRepository extends JpaRepository<ServicePost, String> {

    /**
     * Busca por owner
     */
    List<ServicePost> findByOwnerId(Long ownerId);

    /**
     * Busca por status
     */
    List<ServicePost> findByStatus(PostStatus status);

    /**
     * Busca por ruta y rango de fechas
     */
    @Query("SELECT p FROM ServicePost p WHERE " +
            "p.origin = :origin AND p.destination = :destination AND " +
            "p.departureDateTime BETWEEN :fromDate AND :toDate AND " +
            "p.status = 'PUBLISHED' " +
            "ORDER BY p.departureDateTime ASC")
    List<ServicePost> findByRouteAndDateRange(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    /**
     * Busca por ID con lock pesimista para actualización
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ServicePost p WHERE p.id = :id")
    Optional<ServicePost> findByIdForUpdate(@Param("id") String id);
}
