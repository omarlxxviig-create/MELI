package com.meli.inventory_service.domain.ports.in;

import com.meli.inventory_service.domain.model.ServicePost;
import com.meli.inventory_service.domain.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada para casos de uso de reservas (BookingUseCase)
 */
public interface BookingPort {

    /**
     * Crea una nueva publicación de servicio
     */
    ServicePost createPost(CreateServicePostCommand command);

    /**
     * Publica un post (lo hace visible para reservas)
     */
    ServicePost publishPost(String postId);

    /**
     * Obtiene un post por ID
     */
    Optional<ServicePost> getPostById(String postId);

    /**
     * Lista todos los posts disponibles (publicados)
     */
    List<ServicePost> listAvailablePosts();

    /**
     * Busca posts por ruta y fecha
     */
    List<ServicePost> searchPosts(String origin, String destination, LocalDateTime fromDate, LocalDateTime toDate);

    /**
     * Crea una reserva de asientos
     */
    Reservation createReservation(CreateReservationCommand command);

    /**
     * Cancela una reserva
     */
    Reservation cancelReservation(String reservationId);

    /**
     * Confirma una reserva
     */
    Reservation confirmReservation(String reservationId);

    /**
     * Obtiene una reserva por ID
     */
    Optional<Reservation> getReservationById(String reservationId);

    /**
     * Lista reservas de un usuario
     */
    List<Reservation> getUserReservations(Long userId);

    /**
     * Lista reservas de un post
     */
    List<Reservation> getPostReservations(String postId);

    /**
     * Procesa reservas expiradas (job automático)
     */
    int processExpiredReservations();
}
