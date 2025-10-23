DELETE FROM store_inventory;
DELETE FROM reservation;
DELETE FROM outbox_message;

INSERT INTO store_inventory (store_id, product_id, total_quantity, reserved_quantity, version, updated_at)
VALUES ('store-1', 'sku-1', 1000, 0, 0, CURRENT_TIMESTAMP);
