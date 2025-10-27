package com.meli.inventory_service.infrastructure.persistence.adapter;

import com.meli.inventory_service.domain.model.ServicePost;
import com.meli.inventory_service.domain.model.ServicePost.PostStatus;
import com.meli.inventory_service.domain.ports.out.ServicePostPort;
import com.meli.inventory_service.infrastructure.persistence.spring.ServicePostJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia para ServicePost
 */
@Component
public class ServicePostAdapter implements ServicePostPort {

    private final ServicePostJpaRepository repository;

    public ServicePostAdapter(ServicePostJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ServicePost save(ServicePost post) {
        return repository.save(post);
    }

    @Override
    public Optional<ServicePost> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Optional<ServicePost> findByIdForUpdate(String id) {
        return repository.findByIdForUpdate(id);
    }

    @Override
    public List<ServicePost> findAll() {
        return repository.findAll();
    }

    @Override
    public List<ServicePost> findByOwnerId(Long ownerId) {
        return repository.findByOwnerId(ownerId);
    }

    @Override
    public List<ServicePost> findByStatus(PostStatus status) {
        return repository.findByStatus(status);
    }

    @Override
    public List<ServicePost> findByRouteAndDateRange(String origin, String destination,
            LocalDateTime fromDate, LocalDateTime toDate) {
        return repository.findByRouteAndDateRange(origin, destination, fromDate, toDate);
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
