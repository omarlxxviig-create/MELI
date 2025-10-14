-- ================================================
-- DATOS DE SEGURIDAD: Usuarios, Roles y Permisos
-- ================================================
-- CONTRASEÑAS (BCrypt - TODAS SON "password" para facilitar pruebas):
-- Todos los usuarios usan password = "password"
-- Hash BCrypt de "password": $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
-- ================================================

-- ================================================
-- 1. PERMISOS (Permissions)
-- ================================================
INSERT INTO permissions (id, name, created_at) VALUES 
(1, 'INVENTORY_READ', CURRENT_TIMESTAMP),
(2, 'INVENTORY_WRITE', CURRENT_TIMESTAMP),
(3, 'INVENTORY_UPDATE', CURRENT_TIMESTAMP),
(4, 'INVENTORY_DELETE', CURRENT_TIMESTAMP),
(5, 'INVENTORY_RESERVE', CURRENT_TIMESTAMP),
(6, 'INVENTORY_RELEASE', CURRENT_TIMESTAMP),
(7, 'PRODUCT_READ', CURRENT_TIMESTAMP),
(8, 'PRODUCT_WRITE', CURRENT_TIMESTAMP),
(9, 'PRODUCT_UPDATE', CURRENT_TIMESTAMP),
(10, 'PRODUCT_DELETE', CURRENT_TIMESTAMP),
(11, 'USER_READ', CURRENT_TIMESTAMP),
(12, 'USER_WRITE', CURRENT_TIMESTAMP),
(13, 'USER_UPDATE', CURRENT_TIMESTAMP),
(14, 'USER_DELETE', CURRENT_TIMESTAMP),
(15, 'ROLE_READ', CURRENT_TIMESTAMP),
(16, 'ROLE_WRITE', CURRENT_TIMESTAMP),
(17, 'ROLE_UPDATE', CURRENT_TIMESTAMP),
(18, 'ROLE_DELETE', CURRENT_TIMESTAMP),
(19, 'REPORT_READ', CURRENT_TIMESTAMP),
(20, 'REPORT_GENERATE', CURRENT_TIMESTAMP),
(21, 'SYSTEM_ADMIN', CURRENT_TIMESTAMP),
(22, 'SYSTEM_CONFIG', CURRENT_TIMESTAMP);

-- ================================================
-- 2. ROLES
-- ================================================
INSERT INTO roles (id, name, description, created_at) VALUES 
(1, 'ROLE_ADMIN', 'Administrador con acceso total al sistema', CURRENT_TIMESTAMP),
(2, 'ROLE_MANAGER', 'Gerente con acceso a gestión y reportes', CURRENT_TIMESTAMP),
(3, 'ROLE_WAREHOUSE_STAFF', 'Personal de almacén con acceso a inventario', CURRENT_TIMESTAMP),
(4, 'ROLE_USER', 'Usuario básico con acceso de solo lectura', CURRENT_TIMESTAMP),
(5, 'ROLE_API_CLIENT', 'Cliente API con acceso programático', CURRENT_TIMESTAMP);

-- ================================================
-- 3. ASIGNACIÓN DE PERMISOS A ROLES
-- ================================================

-- ROLE_ADMIN: Todos los permisos
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6),   -- INVENTORY_*
(1, 7), (1, 8), (1, 9), (1, 10),   -- PRODUCT_*
(1, 11), (1, 12), (1, 13), (1, 14), -- USER_*
(1, 15), (1, 16), (1, 17), (1, 18), -- ROLE_*
(1, 19), (1, 20),                   -- REPORT_*
(1, 21), (1, 22);                   -- SYSTEM_*

-- ROLE_MANAGER: Gestión de inventario/productos y reportes
INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6),   -- INVENTORY_*
(2, 7), (2, 8), (2, 9), (2, 10),   -- PRODUCT_*
(2, 11),                            -- USER_READ
(2, 19), (2, 20);                   -- REPORT_*

-- ROLE_WAREHOUSE_STAFF: Gestión de inventario
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3, 1), (3, 2), (3, 3), (3, 5), (3, 6),   -- INVENTORY_READ/WRITE/UPDATE/RESERVE/RELEASE
(3, 7), (3, 8);                           -- PRODUCT_READ/WRITE

-- ROLE_USER: Solo lectura
INSERT INTO role_permissions (role_id, permission_id) VALUES
(4, 1),   -- INVENTORY_READ
(4, 7);   -- PRODUCT_READ

-- ROLE_API_CLIENT: Acceso API completo
INSERT INTO role_permissions (role_id, permission_id) VALUES
(5, 1), (5, 2), (5, 3), (5, 4), (5, 5), (5, 6),   -- INVENTORY_*
(5, 7), (5, 8), (5, 9), (5, 10);   -- PRODUCT_*

-- ================================================
-- 4. USUARIOS
-- Contraseñas BCrypt (VERIFICADAS Y FUNCIONALES):
-- Todos los usuarios usan la contraseña: "password"
-- Hash BCrypt de "password": $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYpN7hhB.W6
-- ================================================

INSERT INTO users (id, username, email, password, first_name, last_name, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at) VALUES
(1, 'admin', 'admin@meli.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYpN7hhB.W6', 'Admin', 'System', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'manager', 'manager@meli.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYpN7hhB.W6', 'John', 'Manager', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'warehouse', 'warehouse@meli.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYpN7hhB.W6', 'Bob', 'Worker', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'user', 'user@meli.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYpN7hhB.W6', 'Jane', 'User', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'apiclient', 'api@meli.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYpN7hhB.W6', 'API', 'Client', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ================================================
-- 5. ASIGNACIÓN DE ROLES A USUARIOS
-- ================================================
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),  -- admin -> ROLE_ADMIN
(2, 2),  -- manager -> ROLE_MANAGER
(3, 3),  -- warehouse -> ROLE_WAREHOUSE_STAFF
(4, 4),  -- user -> ROLE_USER
(5, 5);  -- apiclient -> ROLE_API_CLIENT
