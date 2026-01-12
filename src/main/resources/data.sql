-- ============================================
-- DATA SEEDS VILLADICTOS HOTEL
-- ============================================

-- USUARIOS (Contraseña: "password")
INSERT INTO usuarios (nombre, email, password_hash, rol, telefono) VALUES
('Admin Webmaster', 'admin@hotel.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRy6qo8.gOhNeOEW6ChiuU6jO2', 'webmaster', '600111222'),
('Laura Recepción', 'recepcion@hotel.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRy6qo8.gOhNeOEW6ChiuU6jO2', 'recepcion', '600333444'),
('Juan Cliente', 'juan@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRy6qo8.gOhNeOEW6ChiuU6jO2', 'cliente', '600555666'),
('María García', 'maria@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7I73TZRy6qo8.gOhNeOEW6ChiuU6jO2', 'cliente', '600777888');

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
('Temporada Baja', '2024-01-01', '2024-05-31', 1.00),
('Temporada Media', '2024-06-01', '2024-06-30', 1.25),
('Temporada Alta', '2024-07-01', '2024-09-15', 1.50),
('Navidad', '2024-12-21', '2025-01-06', 1.80);

-- MODELOS DE RESERVA
INSERT INTO modelos_reserva (nombre, descripcion, precio_adicional_noche) VALUES
('Solo Alojamiento', 'Sin comidas', 0.00),
('Alojamiento y Desayuno', 'Incluye desayuno', 15.00),
('Media Pensión', 'Incluye desayuno y cena', 35.00),
('Pensión Completa', 'Incluye desayuno, comida y cena', 50.00);

-- SERVICIOS
INSERT INTO servicios (nombre, descripcion, precio, tipo) VALUES
('SPA - Masaje', 'Masaje 60 minutos', 60.00, 'Por uso'),
('Gimnasio', 'Acceso al gimnasio', 10.00, 'Por día'),
('Parking', 'Plaza cubierta', 15.00, 'Por día'),
('Restaurante', 'Comida', 20.00, 'Por reserva'),
('Transfer', 'Aeropuerto', 40.00, 'Por reserva');

-- RESERVAS
INSERT INTO reservas (id_usuario, id_habitacion, id_sala, id_modelo_reserva, id_temporada, fecha_inicio, fecha_fin, num_personas, precio_total, estado) VALUES
(3, 3, NULL, 3, 3, '2024-07-15', '2024-07-18', 2, 635.00, 'confirmada'),
(4, 6, NULL, 4, 3, '2024-08-10', '2024-08-15', 2, 2065.00, 'confirmada'),
(3, NULL, 1, NULL, 2, '2024-09-20', '2024-09-21', 80, 500.00, 'confirmada');

-- SERVICIOS DE RESERVAS
INSERT INTO reserva_servicios (id_reserva, id_servicio, cantidad, precio_unitario, subtotal) VALUES
(1, 4, 3, 15.00, 45.00),
(1, 2, 2, 25.00, 50.00),
(2, 5, 1, 40.00, 40.00);

-- PAGOS
INSERT INTO pagos (id_reserva, cod_transaccion_tpv, monto, metodo_pago, estado) VALUES
(1, 'TPV_2024071500123', 635.00, 'TPV', 'exitoso'),
(2, 'TPV_2024081000456', 2065.00, 'TPV', 'exitoso'),
(3, 'TPV_2024092000789', 500.00, 'Transferencia', 'exitoso');

-- BLOQUEOS
INSERT INTO bloqueos (id_habitacion, id_sala, fecha_inicio, fecha_fin, motivo, id_usuario_creador) VALUES
(1, NULL, '2024-10-01', '2024-10-05', 'Renovación pintura', 2),
(NULL, 3, '2024-11-15', '2024-11-16', 'Actualización equipamiento', 2);
