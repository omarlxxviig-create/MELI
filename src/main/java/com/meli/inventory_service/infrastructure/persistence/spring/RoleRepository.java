package com.meli.inventory_service.infrastructure.persistence.spring;

import com.meli.inventory_service.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Role.
 * Proporciona métodos para operaciones de persistencia de roles.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Busca un rol por su nombre.
     *
     * @param name nombre del rol
     * @return Optional con el rol si existe
     */
    Optional<Role> findByName(Role.RoleName name);

    /**
     * Verifica si existe un rol con el nombre dado.
     *
     * @param name nombre del rol a verificar
     * @return true si existe, false en caso contrario
     */
    Boolean existsByName(Role.RoleName name);

    /**
     * Busca un rol con sus permisos cargados de forma EAGER.
     *
     * @param name nombre del rol
     * @return Optional con el rol y sus permisos
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") Role.RoleName name);
}
