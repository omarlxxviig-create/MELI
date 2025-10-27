package com.meli.inventory_service.infrastructure.persistence.adapter;

import com.meli.inventory_service.domain.model.Reservation;
import com.meli.inventory_service.domain.model.Reservation.ReservationStatus;
import com.meli.inventory_service.domain.ports.out.ReservationPort;
import com.meli.inventory_service.infrastructure.persistence.spring.ReservationJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia para Reservation
 */
@Component
public class ReservationAdapter implements ReservationPort {

    private final ReservationJpaRepository repository;

    public ReservationAdapter(ReservationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Reservation save(Reservation reservation) {
        return repository.save(reservation);
    }

    @Override
    public Optional<Reservation> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Reservation> findByIdForUpdate(String id) {
        return repository.findByIdForUpdate(id);
    }

    @Override
    public List<Reservation> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Reservation> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public List<Reservation> findByPostId(String postId) {
        return repository.findByPostId(postId);
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        return repository.findByStatus(status);
    }

    @Override
    public List<Reservation> findExpiredReservations(LocalDateTime now) {
        return repository.findExpiredReservations(now);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return repository.existsById(id);
    }
}
