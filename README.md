# Villadictos Hotel - Sistema de Gestión
https://g04-iw-hotel-production.up.railway.app
Sistema de gestión hotelera desarrollado con Spring Boot 3.2.3, MySQL y Thymeleaf.

## Requisitos

- Java 17+
- MySQL 8.0+
- Maven 3.6+ (incluido)

## Instalación Rápida

### 1. Configurar Base de Datos

```bash
mysql -u root -p
```

```sql
CREATE DATABASE villadictos_db;
CREATE USER 'villadictos'@'localhost' IDENTIFIED BY 'villadictos123';
GRANT ALL PRIVILEGES ON villadictos_db.* TO 'villadictos'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 2. Poblar Base de Datos

```bash
mysql -u villadictos -pvilladictos123 villadictos_db < src/main/resources/schema.sql
mysql -u villadictos -pvilladictos123 villadictos_db < src/main/resources/data.sql
```

### 3. Ejecutar Aplicación

```bash
./mvnw spring-boot:run
```

Acceder a: **http://localhost:8080**

## Usuarios de Prueba

| Email | Contraseña | Rol |
|-------|------------|-----|
| admin@hotel.com | password | Webmaster |
| recepcion@hotel.com | password | Recepción |
| juan@gmail.com | password | Cliente |
| maria@gmail.com | password | Cliente |

## Estructura de Base de Datos

### Tablas Principales

- **usuarios** - Usuarios del sistema (webmaster, recepción, cliente)
- **tipos_habitacion** - Tipos de habitaciones (Individual, Doble, Suite...)
- **habitaciones** - Habitaciones del hotel (con campo `destacada`)
- **salas** - Salas de conferencias
- **temporadas** - Temporadas con factores de precio
- **modelos_reserva** - Regímenes alimenticios (Media Pensión, Pensión Completa...)
- **servicios** - Servicios adicionales (SPA, Parking, Transfer...)
- **reservas** - Reservas de habitaciones y salas
- **reserva_servicios** - Servicios contratados por reserva
- **pagos** - Pagos de reservas
- **bloqueos** - Bloqueos de habitaciones/salas

### Diagrama ER

Ver archivo `BDiagrama.svg` para el diagrama completo de relaciones.

## Tecnologías

- Spring Boot 3.2.3
- Spring Security
- Spring Data JPA
- Thymeleaf
- MySQL 8.0
- Bootstrap 5
- Maven
