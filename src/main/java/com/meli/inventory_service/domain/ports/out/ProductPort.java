package com.meli.inventory_service.domain.ports.out;

import com.meli.inventory_service.domain.model.Product;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para operaciones con productos.
 * Sigue el principio de Arquitectura Hexagonal: el dominio define la interfaz,
 * la infraestructura la implementa.
 */
public interface ProductPort {
    Optional<Product> findById(String id);

    Product save(Product product);

    List<Product> findAll();

    void deleteById(String id);
}
