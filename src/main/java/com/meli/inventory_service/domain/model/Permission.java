package com.meli.inventory_service.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad de Permiso para implementar control de acceso granular.
 * Define las acciones específicas que se pueden realizar en el sistema.
 */
@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private PermissionName name;

    @Column(length = 200)
    private String description;

    @Column(length = 50)
    private String resource; // Recurso sobre el que aplica (ej: INVENTORY, PRODUCT, USER)

    @Column(length = 20)
    private String action; // Acción permitida (READ, WRITE, DELETE, UPDATE)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public Permission() {
    }

    public Permission(PermissionName name) {
        this.name = name;
    }

    public Permission(PermissionName name, String description, String resource, String action) {
        this.name = name;
        this.description = description;
        this.resource = resource;
        this.action = action;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PermissionName getName() {
        return name;
    }

    public void setName(PermissionName name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Enum de permisos predefinidos en el sistema.
     */
    public enum PermissionName {
        // Inventory permissions
        INVENTORY_READ,
        INVENTORY_WRITE,
        INVENTORY_UPDATE,
        INVENTORY_DELETE,
        INVENTORY_RESERVE,
        INVENTORY_RELEASE,

        // Product permissions
        PRODUCT_READ,
        PRODUCT_WRITE,
        PRODUCT_UPDATE,
        PRODUCT_DELETE,

        // User management permissions
        USER_READ,
        USER_WRITE,
        USER_UPDATE,
        USER_DELETE,

        // Role management permissions
        ROLE_READ,
        ROLE_WRITE,
        ROLE_UPDATE,
        ROLE_DELETE,

        // Reports and analytics
        REPORT_READ,
        REPORT_GENERATE,

        // System administration
        SYSTEM_ADMIN,
        SYSTEM_CONFIG
    }
}
