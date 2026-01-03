-- ============================================
-- DATA SEEDS VILLADICTOS HOTEL
-- ============================================

-- USUARIOS (Contraseña: "password")
INSERT INTO usuarios (nombre, email, password_hash, rol, telefono) VALUES
('Admin Webmaster', 'admin@hotel.com', '$2a$10$quBqX0kp4GUvKp7qFHeWbOSTGMQHHBUdMS2e.K./OMsa2V/txCrye', 'webmaster', '600111222'),
('Laura Recepción', 'recepcion@hotel.com', '$2a$10$quBqX0kp4GUvKp7qFHeWbOSTGMQHHBUdMS2e.K./OMsa2V/txCrye', 'recepcion', '600333444'),
('Juan Cliente', 'juan@gmail.com', '$2a$10$quBqX0kp4GUvKp7qFHeWbOSTGMQHHBUdMS2e.K./OMsa2V/txCrye', 'cliente', '600555666'),
('María García', 'maria@gmail.com', '$2a$10$quBqX0kp4GUvKp7qFHeWbOSTGMQHHBUdMS2e.K./OMsa2V/txCrye', 'cliente', '600777888');

-- TIPOS DE HABITACIÓN
INSERT INTO tipos_habitacion (nombre, descripcion, capacidad_personas, precio_base_noche) VALUES
('Individual', 'Habitación con cama individual', 1, 50.00),
('Doble Estándar', 'Habitación con cama doble', 2, 80.00),
('Doble Superior', 'Habitación espaciosa', 2, 110.00),
('Suite Junior', 'Suite con salón', 3, 150.00),
('Suite Premium', 'Suite de lujo', 4, 250.00);

-- HABITACIONES
INSERT INTO habitaciones (numero_habitacion, planta, vistas, id_tipo, destacada) VALUES
('101', 1, 'Calle', 1, FALSE),
('102', 1, 'Jardín', 2, FALSE),
('201', 2, 'Mar', 3, TRUE),
('202', 2, 'Mar', 3, TRUE),
('301', 3, 'Mar', 4, TRUE),
('303', 3, 'Mar', 5, TRUE);

-- SALAS
INSERT INTO salas (nombre, aforo_max, descripcion, equipamiento, precio_base_dia) VALUES
('Gran Conferencia', 100, 'Sala principal', 'Proyector 4K, Sonido', 500.00),
('Ejecutiva', 20, 'Sala corporativa', 'TV 65", Videoconferencia', 200.00),
('Privada', 10, 'Sala pequeña', 'TV 50", WiFi', 150.00);

-- TEMPORADAS
INSERT INTO temporadas (nombre, fecha_inicio, fecha_fin, factor_precio) VALUES
('Temporada Baja', '2025-01-01', '2025-05-31', 1.00),
('Temporada Media', '2025-06-01', '2025-06-30', 1.25),
('Temporada Alta', '2025-07-01', '2025-09-15', 1.50),
('Navidad', '2025-12-21', '2026-01-06', 1.80);

-- MODELOS DE RESERVA
INSERT INTO modelos_reserva (nombre, descripcion, precio_adicional_noche) VALUES
('Solo Alojamiento', 'Sin comidas', 0.00),
('Alojamiento y Desayuno', 'Incluye desayuno', 15.00),
('Media Pensión', 'Desayuno y cena', 35.00),
('Pensión Completa', 'Todas las comidas', 50.00),
('Todo Incluido', 'Comidas y bebidas', 75.00);

-- SERVICIOS
INSERT INTO servicios (nombre, descripcion, precio, tipo) VALUES
('SPA - Masaje', 'Masaje 60 minutos', 60.00, 'Por uso'),
('SPA - Circuito', 'Circuito termal', 25.00, 'Por día'),
('Gimnasio', 'Acceso al gimnasio', 10.00, 'Por día'),
('Parking', 'Plaza cubierta', 15.00, 'Por día'),
('Transfer', 'Aeropuerto', 40.00, 'Por reserva'),
('Cuna Bebé', 'Cuna en habitación', 10.00, 'Por reserva');

-- RESERVAS (Fechas actualizadas a finales de 2025 y principios de 2026)
INSERT INTO reservas (id_usuario, id_habitacion, id_sala, id_modelo_reserva, id_temporada, fecha_inicio, fecha_fin, num_personas, precio_total, estado) VALUES
(3, 3, NULL, 3, 4, '2025-12-28', '2026-01-02', 2, 635.00, 'confirmada'),
(4, 6, NULL, 5, 4, '2026-01-05', '2026-01-10', 2, 2065.00, 'confirmada'),
(3, NULL, 1, NULL, 1, '2026-01-15', '2026-01-16', 80, 500.00, 'confirmada'),
(3, 1, NULL, 2, 1, '2025-11-10', '2025-11-15', 1, 325.00, 'completada'),
(4, 2, NULL, 2, 1, '2025-11-20', '2025-11-22', 2, 190.00, 'completada'),
(3, 4, NULL, 4, 4, '2025-12-05', '2025-12-10', 3, 1000.00, 'completada'),
(4, 5, NULL, 5, 4, '2025-12-15', '2025-12-20', 4, 1625.00, 'completada'),
(3, 1, NULL, 1, 4, '2025-12-22', '2025-12-24', 1, 100.00, 'completada'),
(4, 3, NULL, 3, 1, '2026-01-20', '2026-01-25', 2, 725.00, 'confirmada'),
(3, 2, NULL, 2, 1, '2026-02-01', '2026-02-05', 2, 380.00, 'confirmada'),
(4, 4, NULL, 4, 1, '2026-02-10', '2026-02-15', 3, 1200.00, 'confirmada'),
(3, 5, NULL, 5, 1, '2026-03-01', '2026-03-05', 4, 1800.00, 'confirmada'),
(4, 1, NULL, 1, 1, '2025-11-01', '2025-11-05', 1, 250.00, 'confirmada'),
(3, 3, NULL, 3, 1, '2025-11-25', '2025-11-28', 2, 450.00, 'confirmada'),
(4, 6, NULL, 5, 4, '2025-12-30', '2026-01-03', 2, 2500.00, 'confirmada');

-- SERVICIOS DE RESERVAS
INSERT INTO reserva_servicios (id_reserva, id_servicio, cantidad, precio_unitario, subtotal) VALUES
(1, 4, 3, 15.00, 45.00),
(1, 2, 2, 25.00, 50.00),
(2, 5, 1, 40.00, 40.00),
(4, 2, 1, 25.00, 25.00),
(5, 4, 2, 15.00, 30.00),
(6, 1, 2, 60.00, 120.00),
(7, 3, 5, 10.00, 50.00),
(9, 5, 1, 40.00, 40.00),
(11, 6, 1, 10.00, 10.00);

-- PAGOS
INSERT INTO pagos (id_reserva, cod_transaccion_tpv, monto, metodo_pago, estado) VALUES
(1, 'TPV_2025122800123', 635.00, 'TPV', 'exitoso'),
(2, 'TPV_2026010500456', 2065.00, 'TPV', 'exitoso'),
(3, 'TPV_2026011500789', 500.00, 'Transferencia', 'exitoso'),
(4, 'TPV_2025111000111', 325.00, 'TPV', 'exitoso'),
(5, 'TPV_2025112000222', 190.00, 'Efectivo', 'exitoso'),
(6, 'TPV_2025120500333', 1000.00, 'TPV', 'exitoso'),
(7, 'TPV_2025121500444', 1625.00, 'TPV', 'exitoso'),
(8, 'TPV_2025122200555', 100.00, 'Efectivo', 'exitoso'),
(9, 'TPV_2026012000666', 725.00, 'TPV', 'exitoso'),
(10, 'TPV_2026020100777', 380.00, 'TPV', 'exitoso'),
(11, 'TPV_2026021000888', 1200.00, 'Transferencia', 'exitoso'),
(12, 'TPV_2026030100999', 1800.00, 'TPV', 'exitoso'),
(13, 'TPV_2025110100000', 250.00, 'TPV', 'exitoso'),
(14, 'TPV_2025112500111', 450.00, 'Efectivo', 'exitoso'),
(15, 'TPV_2025123000222', 2500.00, 'TPV', 'exitoso');

-- BLOQUEOS
INSERT INTO bloqueos (id_habitacion, id_sala, fecha_inicio, fecha_fin, motivo, id_usuario_creador) VALUES
(1, NULL, '2025-10-01', '2025-10-05', 'Renovación pintura', 2),
(NULL, 3, '2025-11-15', '2025-11-16', 'Actualización equipamiento', 2),
(2, NULL, '2026-01-20', '2026-01-22', 'Fuga de agua', 2),
(3, NULL, '2026-03-10', '2026-03-12', 'Limpieza profunda', 2);
