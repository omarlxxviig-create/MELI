package com.meli.inventory_service.application.service;

import com.meli.inventory_service.domain.model.Product;
import com.meli.inventory_service.domain.ports.out.ProductPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de aplicación para gestión de productos.
 * Coordina las operaciones de dominio usando el puerto ProductPort.
 */
@Service
public class ProductService {
    private final ProductPort productPort;

    public ProductService(ProductPort productPort) {
        this.productPort = productPort;
    }

    public List<Product> findAll() {
        return productPort.findAll();
    }

    public Optional<Product> findById(String id) {
        return productPort.findById(id);
    }

    @Transactional
    public Product create(Product p) {
        if (p.getId() == null || p.getId().isBlank())
            p.setId(UUID.randomUUID().toString());
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        return productPort.save(p);
    }

    @Transactional
    public Product update(String id, Product patch) {
        return productPort.findById(id).map(existing -> {
            if (patch.getName() != null)
                existing.setName(patch.getName());
            if (patch.getSku() != null)
                existing.setSku(patch.getSku());
            if (patch.getDescription() != null)
                existing.setDescription(patch.getDescription());
            existing.setUpdatedAt(Instant.now());
            return productPort.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    @Transactional
    public void delete(String id) {
        productPort.deleteById(id);
    }
}
