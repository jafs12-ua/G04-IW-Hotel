# Gestión de Hotel VillaDictos

## 1. Información de Contacto

**Responsable:** Sergio Mínguez Berja  
**Email:** smb96@alu.ua.es

---

## 2. Explicación de la API

En este documento se detalla el uso e implementación de las APIs del hotel VillaDictos.

Se ofrecen servicios para la consulta pública de nuestra oferta comercial (habitaciones, salas de eventos y servicios adicionales) y un servicio para iniciar el proceso de reserva con validación de disponibilidad.

La finalidad de la API es permitir que sistemas externos (como comparadores de hoteles, agencias de viajes o aplicaciones de turismo) puedan obtener información actualizada sobre nuestras habitaciones, precios por temporada, disponibilidad y redirigir a los usuarios al túnel de reservas.

---

## 3. URL Base y Autenticación

### URL Base
La API se encuentra actualmente en entorno de desarrollo local, no la tenemos alojada en ningún servidor.
```
http://localhost:8080/api/v1
```
*(Nota: Se actualizará cuando se realice el despliegue en el servidor de producción).*

### Autenticación
**Todos los endpoints de esta API son públicos** y no requieren autenticación.

---

## 4. Listado de End-Points

A continuación se detallan los recursos disponibles.

### 4.1. Obtener Listado de Habitaciones (No Securizada)

Devuelve el catálogo de habitaciones disponibles con posibilidad de filtrar por fechas, precio y temporada. Útil para buscadores y comparadores.

**Ruta:** `/rooms`  
**Método:** `GET`

**Parámetros (Query Params):**

| Nombre | Tipo | Rango/Formato | Descripción |
|--------|------|---------------|-------------|
| checkIn | String | YYYY-MM-DD | (Opcional) Fecha de entrada |
| checkOut | String | YYYY-MM-DD | (Opcional) Fecha de salida |
| minPrice | Number | >= 0 | (Opcional) Precio mínimo por noche |
| maxPrice | Number | >= minPrice | (Opcional) Precio máximo por noche |
| season | String | ALTA, MEDIA, BAJA | (Opcional) Temporada |
| page | Integer | >= 0 | (Opcional) Número de página, default: 0 |
| size | Integer | 1-100 | (Opcional) Elementos por página, default: 10 |

**Ejemplo de Petición:**
```
BASE_URL/api/v1/rooms?checkIn=2025-12-20&checkOut=2025-12-23&minPrice=50&maxPrice=150
```

**Estructura de la Respuesta (JSON):**
```json
{
    "content": [
        {
            "id": 1,
            "number": "101",
            "type": "Doble Estándar",
            "capacity": 2,
            "pricePerNight": 85.00,
            "season": "ALTA",
            "available": true,
            "amenities": ["WiFi", "TV", "Minibar"]
        }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 6,
    "totalPages": 1
}
```

---

### 4.2. Detalle de Habitación (No Securizada)

Devuelve la información completa de una habitación específica.

**Ruta:** `/rooms/{id}`  
**Método:** `GET`

**Parámetros:**
| Nombre | Tipo | Descripción |
|--------|------|-------------|
| id | Integer | Identificador de la habitación |

**Estructura de la Respuesta (JSON):**
```json
{
    "id": 1,
    "number": "101",
    "type": "Doble Estándar",
    "capacity": 2,
    "size": 25,
    "pricePerNight": 80.00,
    "season": "BAJA",
    "description": "Habitación con cama doble",
    "amenities": ["WiFi", "TV", "Aire acondicionado", "Minibar"],
    "images": ["https://hotel.com/images/room-101-1.jpg"],
    "available": true,
    "floor": 1
}
```

**Gestión de Errores:**
- **404 Not Found:** La habitación no existe.

---

### 4.3. Obtener Listado de Salas (No Securizada)

Devuelve el catálogo de salas de conferencias y espacios comunes.

**Ruta:** `/facilities`  
**Método:** `GET`

**Parámetros (Query Params):**
| Nombre | Tipo | Descripción |
|--------|------|-------------|
| minCapacity | Integer | (Opcional) Capacidad mínima |

**Estructura de la Respuesta (JSON):**
```json
[
    {
        "id": 1,
        "name": "Gran Conferencia",
        "type": "CONFERENCE_ROOM",
        "capacity": 100,
        "pricePerHour": 62.50,
        "description": "Sala principal para eventos",
        "amenities": ["Proyector 4K", "Sistema de sonido"],
        "schedule": {"openTime": "08:00", "closeTime": "22:00"},
        "available": true
    }
]
```

---

### 4.4. Detalle de Sala (No Securizada)

Devuelve la información completa de una sala específica.

**Ruta:** `/facilities/{id}`  
**Método:** `GET`

**Parámetros:**
| Nombre | Tipo | Descripción |
|--------|------|-------------|
| id | Integer | Identificador de la sala |

**Gestión de Errores:**
- **404 Not Found:** La sala no existe.

---

### 4.5. Obtener Listado de Servicios (No Securizada)

Devuelve el catálogo de servicios adicionales del hotel (SPA, parking, transfer, etc.).

**Ruta:** `/services`  
**Método:** `GET`

**Estructura de la Respuesta (JSON):**
```json
[
    {
        "id": 1,
        "name": "SPA - Masaje",
        "description": "Masaje relajante de 60 minutos",
        "price": 60.00,
        "type": "Por uso"
    },
    {
        "id": 2,
        "name": "Parking",
        "description": "Plaza de aparcamiento cubierta",
        "price": 15.00,
        "type": "Por día"
    }
]
```

---

### 4.6. Detalle de Servicio (No Securizada)

Devuelve la información de un servicio específico.

**Ruta:** `/services/{id}`  
**Método:** `GET`

**Parámetros:**
| Nombre | Tipo | Descripción |
|--------|------|-------------|
| id | Integer | Identificador del servicio |

**Gestión de Errores:**
- **404 Not Found:** El servicio no existe.

---

### 4.7. Iniciar Reserva (No Securizada)

Valida la disponibilidad de una habitación y devuelve la URL de redirección al túnel de reservas. Útil para integraciones que quieran enviar usuarios a completar una reserva.

**Ruta:** `/bookings/start`  
**Método:** `GET`

**Parámetros (Query Params):**
| Nombre | Tipo | Rango/Formato | Descripción |
|--------|------|---------------|-------------|
| roomId | Integer | ID válido | ID de la habitación a reservar |
| checkIn | String | YYYY-MM-DD | Fecha de entrada |
| checkOut | String | YYYY-MM-DD | Fecha de salida (posterior a checkIn) |
| guests | Integer | 1-10 | Número de huéspedes |

**Ejemplo de Petición:**
```
BASE_URL/api/v1/bookings/start?roomId=1&checkIn=2025-12-20&checkOut=2025-12-23&guests=2
```

**Estructura de la Respuesta (JSON) - Caso Éxito (200 OK):**
```json
{
    "roomId": 1,
    "roomNumber": "101",
    "roomType": "Doble Estándar",
    "checkIn": "2025-12-20",
    "checkOut": "2025-12-23",
    "guests": 2,
    "redirectUrl": "/reservas/nueva?habitacion=1&checkIn=2025-12-20&checkOut=2025-12-23&personas=2"
}
```

**Gestión de Errores:**
- **400 Bad Request:** Fechas inválidas o capacidad de habitación excedida.
- **404 Not Found:** La habitación no existe.
- **409 Conflict:** La habitación no está disponible para las fechas indicadas.

**Ejemplo de uso:**
Si una agencia de viajes quiere ofrecer reservas en VillaDictos, puede usar este endpoint para validar disponibilidad y obtener el enlace directo al túnel de reservas.
