package com.meli.inventory_service.application.dto;

/**
 * DTO para solicitudes de reserva de inventario.
 * Pertenece a la capa de aplicación para desacoplar
 * la API REST del dominio.
 */
public class ReserveRequest {
    private String storeId;
    private String productId;
    private int quantity;
    private String transactionId;

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
