package com.meli.inventory_service.infrastructure.persistence;

import com.meli.inventory_service.domain.ports.out.ProductPort;
import com.meli.inventory_service.domain.model.Product;
import com.meli.inventory_service.infrastructure.persistence.spring.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador JPA para el puerto ProductPort.
 * Implementa las operaciones de persistencia usando Spring Data JPA.
 */
@Component
public class JpaProductAdapter implements ProductPort {
    private final ProductRepository repository;

    public JpaProductAdapter(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Product> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Product save(Product product) {
        return repository.save(product);
    }

    @Override
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
