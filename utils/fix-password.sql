-- Actualizar el hash BCrypt para la contraseña "admin123"
UPDATE users 
SET password = '$2a$12$Ilp6TS9ehfIsBRPIuLGpReRNSYt1IBpSK3FS4Oso4mUbNKMj9SEDO' 
WHERE username = 'testadmin';

-- Actualizar también el usuario admin para usar la contraseña "password" si es necesario
UPDATE users 
SET password = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYpN7hhB.W6' 
WHERE username = 'admin';

-- Verificar la actualización
SELECT username, SUBSTRING(password, 1, 10) || '...' as password_hash, enabled
FROM users
WHERE username IN ('admin', 'testadmin');

-- Verificar los roles
SELECT u.username, r.name 
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON r.id = ur.role_id
WHERE u.username = 'testadmin';
