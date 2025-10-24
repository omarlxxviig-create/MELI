-- Limpiar datos existentes
DELETE FROM store_inventory;
DELETE FROM product;
DELETE FROM reservation;
DELETE FROM outbox_message;

-- Insertar productos de demo
INSERT INTO product (product_id, sku, name, description, price, stock, created_at, updated_at)
VALUES 
('prod-001', 'LAP-LEN-001', 'Laptop Lenovo ThinkPad', 'Laptop para desarrollo', 2500.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('prod-002', 'LAP-MAC-001', 'MacBook Pro M2', 'Laptop Apple M2', 3500.00, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insertar inventario inicial
INSERT INTO store_inventory (store_id, product_id, total_quantity, reserved_quantity, version, updated_at)
VALUES 
('store-001', 'prod-001', 70000, 0, 0, CURRENT_TIMESTAMP),
('store-002', 'prod-001', 5, 0, 0, CURRENT_TIMESTAMP);
