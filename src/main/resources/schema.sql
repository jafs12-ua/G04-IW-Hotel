-- ============================================
-- SCHEMA VILLADICTOS HOTEL - VERSIÓN FINAL
-- Basado en BDiagrama.svg con cambios solicitados
-- ============================================

DROP TABLE IF EXISTS pagos;
DROP TABLE IF EXISTS reserva_servicios;
DROP TABLE IF EXISTS servicios;
DROP TABLE IF EXISTS detalle_res_habitacion;
DROP TABLE IF EXISTS detalle_res_sala;
DROP TABLE IF EXISTS reservas;
DROP TABLE IF EXISTS modelos_reserva;
DROP TABLE IF EXISTS bloqueos;
DROP TABLE IF EXISTS habitaciones;
DROP TABLE IF EXISTS tipos_habitacion;
DROP TABLE IF EXISTS salas;
DROP TABLE IF EXISTS temporadas;
DROP TABLE IF EXISTS usuarios;

-- ============================================
-- TABLA: USUARIOS
-- ============================================
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol ENUM('webmaster', 'recepcion', 'cliente') NOT NULL,
    telefono VARCHAR(20),
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- TABLA: TIPOS_HABITACION
-- ============================================
CREATE TABLE tipos_habitacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    capacidad_personas INT NOT NULL,
    precio_base_noche DECIMAL(10, 2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- TABLA: HABITACIONES
-- ============================================
CREATE TABLE habitaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_habitacion VARCHAR(20) NOT NULL UNIQUE,
    planta INT NOT NULL,
    vistas VARCHAR(100),
    id_tipo BIGINT NOT NULL,
    destacada BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id_tipo) REFERENCES tipos_habitacion(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- TABLA: SALAS
-- ============================================
CREATE TABLE salas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    aforo_max INT NOT NULL,
    descripcion TEXT,
    equipamiento TEXT,
    precio_base_dia DECIMAL(10, 2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- TABLA: TEMPORADAS
-- ============================================
CREATE TABLE temporadas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    factor_precio DECIMAL(5, 2) NOT NULL DEFAULT 1.00,
    CONSTRAINT chk_temp_fechas CHECK (fecha_fin >= fecha_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- TABLA: MODELOS_RESERVA (NUEVA)
-- ============================================
CREATE TABLE modelos_reserva (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    precio_adicional_noche DECIMAL(10, 2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- TABLA: SERVICIOS (NUEVA)
-- ============================================
CREATE TABLE servicios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    precio DECIMAL(10, 2) NOT NULL,
    tipo ENUM('Por día', 'Por reserva', 'Por uso') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- TABLA: RESERVAS
-- ============================================
CREATE TABLE reservas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    id_habitacion BIGINT NULL,
    id_sala BIGINT NULL,
    id_modelo_reserva BIGINT NULL,
    id_temporada BIGINT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    num_personas INT NOT NULL DEFAULT 1,
    precio_total DECIMAL(10, 2) NOT NULL,
    estado ENUM('pendiente', 'confirmada', 'cancelada', 'completada') NOT NULL DEFAULT 'pendiente',
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    notas TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id),
    FOREIGN KEY (id_habitacion) REFERENCES habitaciones(id),
    FOREIGN KEY (id_sala) REFERENCES salas(id),
    FOREIGN KEY (id_modelo_reserva) REFERENCES modelos_reserva(id),
    FOREIGN KEY (id_temporada) REFERENCES temporadas(id),
    CONSTRAINT chk_recurso CHECK ((id_habitacion IS NOT NULL AND id_sala IS NULL) OR (id_habitacion IS NULL AND id_sala IS NOT NULL)),
    CONSTRAINT chk_fechas CHECK (fecha_fin > fecha_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- TABLA: RESERVA_SERVICIOS (NUEVA)
-- ============================================
CREATE TABLE reserva_servicios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_reserva BIGINT NOT NULL,
    id_servicio BIGINT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    fecha_inicio DATE,
    fecha_fin DATE,
    FOREIGN KEY (id_reserva) REFERENCES reservas(id),
    FOREIGN KEY (id_servicio) REFERENCES servicios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- TABLA: PAGOS
-- ============================================
CREATE TABLE pagos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_reserva BIGINT NOT NULL,
    cod_transaccion_tpv VARCHAR(100) UNIQUE,
    monto DECIMAL(10, 2) NOT NULL,
    metodo_pago ENUM('TPV', 'Transferencia', 'Efectivo') NOT NULL,
    estado ENUM('pendiente', 'exitoso', 'fallido', 'reembolsado') NOT NULL DEFAULT 'pendiente',
    fecha_pago DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_reserva) REFERENCES reservas(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- TABLA: BLOQUEOS
-- ============================================
CREATE TABLE bloqueos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_habitacion BIGINT NULL,
    id_sala BIGINT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    motivo TEXT,
    id_usuario_creador BIGINT NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_habitacion) REFERENCES habitaciones(id),
    FOREIGN KEY (id_sala) REFERENCES salas(id),
    FOREIGN KEY (id_usuario_creador) REFERENCES usuarios(id),
    CONSTRAINT chk_bloqueo_recurso CHECK ((id_habitacion IS NOT NULL AND id_sala IS NULL) OR (id_habitacion IS NULL AND id_sala IS NOT NULL)),
    CONSTRAINT chk_bloqueo_fechas CHECK (fecha_fin >= fecha_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
