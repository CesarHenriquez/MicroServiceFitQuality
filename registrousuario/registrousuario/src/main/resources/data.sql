-- ######################################################################
-- # FASE 1: INSERCIÓN DE ROLES BASE
-- ######################################################################
-- Asegura que los roles base (CLIENTE, DELIVERY, ADMINISTRADOR) existan.
INSERT IGNORE INTO rol (rol_id, nombre) VALUES (1, 'CLIENTE');
INSERT IGNORE INTO rol (rol_id, nombre) VALUES (2, 'DELIVERY');
INSERT IGNORE INTO rol (rol_id, nombre) VALUES (3, 'ADMINISTRADOR');

-- ######################################################################
-- # FASE 2: CREACIÓN DEL USUARIO ADMINISTRADOR INICIAL (admin1)
-- ######################################################################
-- 1. Define el hash de tu clave "1234"
SET @ADMIN_CLAVE_ENCRIPTADA = '$2a$10$xM5sJhMPgpAmd66RNB7jOO/nqcArrMGPJpMfvnx4OSOz1Pd0HsfYS'; 

-- 2. Inserta el usuario admin1 (ID 1, Rol 3) solo si su nickname no existe
INSERT INTO usuarios (usuario_id, nickname, clave, correo, rol_id)
SELECT 1, 'admin1', @ADMIN_CLAVE_ENCRIPTADA, 'admin@bootstrap.com', 3
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE nickname = 'admin1');