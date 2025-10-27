package com.meli.inventory_service.domain.ports.out;

import com.meli.inventory_service.domain.model.ServicePost;
import com.meli.inventory_service.domain.model.ServicePost.PostStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de ServicePost
 */
public interface ServicePostPort {

    /**
     * Guarda un post (create o update)
     */
    ServicePost save(ServicePost post);

    /**
     * Busca por ID
     */
    Optional<ServicePost> findById(String id);

    /**
     * Busca por ID con lock para actualización
     */
    Optional<ServicePost> findByIdForUpdate(String id);

    /**
     * Lista todos los posts
     */
    List<ServicePost> findAll();

    /**
     * Busca posts por owner
     */
    List<ServicePost> findByOwnerId(Long ownerId);

    /**
     * Busca posts por status
     */
    List<ServicePost> findByStatus(PostStatus status);

    /**
     * Busca posts por ruta y rango de fechas
     */
    List<ServicePost> findByRouteAndDateRange(String origin, String destination,
            LocalDateTime fromDate, LocalDateTime toDate);

    /**
     * Elimina un post
     */
    void delete(String id);

    /**
     * Verifica si existe un post
     */
    boolean existsById(String id);
}
