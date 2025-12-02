DROP TABLE IF EXISTS bloqueos;
DROP TABLE IF EXISTS pagos;
DROP TABLE IF EXISTS detalle_res_habitacion;
DROP TABLE IF EXISTS reservas;
DROP TABLE IF EXISTS temporadas;
DROP TABLE IF EXISTS salas;
DROP TABLE IF EXISTS habitaciones;
DROP TABLE IF EXISTS tipos_habitacion;
DROP TABLE IF EXISTS usuarios;

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    telefono VARCHAR(20)
);

-- Tabla de Tipos de Habitación
CREATE TABLE IF NOT EXISTS tipos_habitacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    capacidad_personas INT NOT NULL,
    precio_base_noche DECIMAL(10, 2) NOT NULL
);

-- Tabla de Habitaciones
CREATE TABLE IF NOT EXISTS habitaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_habitacion VARCHAR(20) NOT NULL,
    planta INT NOT NULL,
    vistas VARCHAR(100),
    id_tipo BIGINT NOT NULL,
    activa BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (id_tipo) REFERENCES tipos_habitacion(id)
);

-- Tabla de Salas
CREATE TABLE IF NOT EXISTS salas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    aforo_max INT NOT NULL,
    descripcion TEXT,
    equipamiento TEXT,
    precio_base_dia DECIMAL(10, 2) NOT NULL,
    activa BOOLEAN DEFAULT TRUE
);

-- Tabla de Temporadas
CREATE TABLE IF NOT EXISTS temporadas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    factor_precio DECIMAL(5, 2) NOT NULL
);

-- Tabla de Reservas
CREATE TABLE IF NOT EXISTS reservas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    estado VARCHAR(50) NOT NULL,
    total_calculado DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
);

-- Tabla de Detalle Reserva Habitación
CREATE TABLE IF NOT EXISTS detalle_res_habitacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_reserva BIGINT NOT NULL,
    id_habitacion BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    precio_noche_pactado DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (id_reserva) REFERENCES reservas(id),
    FOREIGN KEY (id_habitacion) REFERENCES habitaciones(id)
);

-- Tabla de Pagos
CREATE TABLE IF NOT EXISTS pagos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_reserva BIGINT NOT NULL,
    cod_transaccion_tpv VARCHAR(100),
    monto DECIMAL(10, 2) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    FOREIGN KEY (id_reserva) REFERENCES reservas(id)
);

-- Tabla de Bloqueos
CREATE TABLE IF NOT EXISTS bloqueos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_recurso BIGINT NOT NULL,
    tipo_recurso VARCHAR(50) NOT NULL, -- 'habitacion' o 'sala'
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    motivo TEXT,
    id_usuario_creador BIGINT NOT NULL,
    FOREIGN KEY (id_usuario_creador) REFERENCES usuarios(id)
);
