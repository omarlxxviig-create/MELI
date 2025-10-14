package com.meli.inventory_service.infrastructure.persistence.spring;

import com.meli.inventory_service.domain.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Permission.
 * Proporciona métodos para operaciones de persistencia de permisos.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Busca un permiso por su nombre.
     *
     * @param name nombre del permiso
     * @return Optional con el permiso si existe
     */
    Optional<Permission> findByName(Permission.PermissionName name);

    /**
     * Verifica si existe un permiso con el nombre dado.
     *
     * @param name nombre del permiso a verificar
     * @return true si existe, false en caso contrario
     */
    Boolean existsByName(Permission.PermissionName name);
}
