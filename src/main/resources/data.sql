-- SEEDS: USUARIOS
-- Passwords ficticios (en producción usa hash real como bcrypt)
INSERT INTO usuarios (nombre, email, password_hash, rol, telefono) VALUES
('Admin Webmaster', 'admin@hotel.com', 'hash_admin_123', 'webmaster', '600111222'),
('Laura Recepción', 'recepcion@hotel.com', 'hash_recep_123', 'recepcion', '600333444'),
('Juan Cliente', 'juan@gmail.com', 'hash_juan_123', 'cliente', '600555666');

-- SEEDS: TIPOS DE HABITACIÓN
INSERT INTO tipos_habitacion (nombre, descripcion, capacidad_personas, precio_base_noche) VALUES
('Individual', 'Habitación acogedora para una persona', 1, 50.00),
('Doble', 'Dos camas o cama de matrimonio', 2, 80.00),
('Suite', 'Lujo, vistas al mar y jacuzzi', 4, 150.00);

-- SEEDS: HABITACIONES
-- Asumiendo que IDs de tipos son 1, 2, 3 respectivamente
INSERT INTO habitaciones (numero_habitacion, planta, vistas, id_tipo, activa) VALUES
('101', 1, 'Calle', 1, 1), -- Individual
('102', 1, 'Jardín', 2, 1), -- Doble
('201', 2, 'Mar', 3, 1),    -- Suite
('202', 2, 'Mar', 2, 1);    -- Doble

-- SEEDS: SALAS
INSERT INTO salas (nombre, aforo_max, descripcion, equipamiento, precio_base_dia, activa) VALUES
('Sala Gran Conferencia', 100, 'Para grandes eventos', 'Proyector 4K, Sonido Sorround', 500.00, 1),
('Sala Reuniones', 10, 'Privada y pequeña', 'Pizarra, TV', 150.00, 1);

-- SEEDS: TEMPORADAS
INSERT INTO temporadas (nombre, fecha_inicio, fecha_fin, factor_precio) VALUES
('Temporada Baja', '2024-01-01', '2024-05-31', 1.00),
('Temporada Alta', '2024-06-01', '2024-09-15', 1.50); -- 50% más caro

-- SEEDS: EJEMPLO DE FLUJO DE RESERVA
-- 1. Crear Reserva (Usuario Juan)
INSERT INTO reservas (id_usuario, estado, total_calculado) VALUES
(3, 'confirmada', 240.00); -- Supongamos 3 noches a 80€

-- 2. Detalle de la reserva (Habitación 102 - Doble)
-- Usamos LAST_INSERT_ID() para coger el ID de la reserva creada arriba
INSERT INTO detalle_res_habitacion (id_reserva, id_habitacion, fecha_inicio, fecha_fin, precio_noche_pactado) VALUES
(LAST_INSERT_ID(), 2, '2024-07-01', '2024-07-04', 80.00);

-- 3. Pago de esa reserva
INSERT INTO pagos (id_reserva, cod_transaccion_tpv, monto, estado) VALUES
(LAST_INSERT_ID(), 'TPV_AB123456', 240.00, 'exitoso');

-- SEEDS: EJEMPLO DE BLOQUEO (Mantenimiento)
-- Recepcionista (ID 2) bloquea la habitación 101 por pintura
INSERT INTO bloqueos (id_recurso, tipo_recurso, fecha_inicio, fecha_fin, motivo, id_usuario_creador) VALUES
(1, 'habitacion', '2024-10-01', '2024-10-05', 'Pintura y arreglos', 2);
