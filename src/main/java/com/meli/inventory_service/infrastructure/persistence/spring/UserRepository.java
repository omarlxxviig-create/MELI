package com.meli.inventory_service.infrastructure.persistence.spring;

import com.meli.inventory_service.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad User.
 * Proporciona métodos para operaciones de persistencia de usuarios.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username nombre de usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por su email.
     *
     * @param email email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el nombre de usuario dado.
     *
     * @param username nombre de usuario a verificar
     * @return true si existe, false en caso contrario
     */
    Boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el email dado.
     *
     * @param email email a verificar
     * @return true si existe, false en caso contrario
     */
    Boolean existsByEmail(String email);

    /**
     * Busca un usuario con sus roles cargados de forma EAGER.
     *
     * @param username nombre de usuario
     * @return Optional con el usuario y sus roles
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    /**
     * Busca usuarios activos (enabled = true).
     *
     * @return lista de usuarios activos
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    java.util.List<User> findAllActiveUsers();
}
