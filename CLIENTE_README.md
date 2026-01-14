# Área de Cliente - Villadictos Hotel

## 📋 Descripción

Se han creado las páginas del área de cliente manteniendo la estética y estilo existentes del proyecto. Todas las páginas son completamente funcionales y están integradas con el backend.

## 🎨 Características

### Páginas Implementadas

1. **Reservas Activas** (`/cliente/reservas-pendientes`)
   - Visualización de reservas pendientes y confirmadas
   - Opción para cancelar reservas
   - Modal de confirmación antes de cancelar
   - Estado visual de cada reserva (pendiente/confirmada)

2. **Histórico de Reservas** (`/cliente/historico`)
   - Visualización de reservas completadas y canceladas
   - Histórico completo ordenado por fecha
   - Información detallada de cada reserva

3. **Mis Datos** (`/cliente/mis-datos`)
   - Actualización de datos personales (nombre, email, teléfono)
   - Cambio de contraseña
   - Validación de formularios
   - Confirmación de contraseña

4. **Nueva Reserva** (`/cliente/nueva-reserva`)
   - Formulario completo para crear reservas
   - Selección de habitación con información de precios
   - Selección de fechas con validación
   - Selección de modelo de reserva (alojamiento, pensión, etc.)
   - Campo para notas adicionales
   - Panel resumen con información del cliente
   - Validaciones JavaScript para fechas

## 🔧 Componentes Técnicos

### Backend

#### Controlador
- **ClienteController.java**: Gestiona todas las rutas del área de cliente
  - `/cliente` - Dashboard (redirige a reservas pendientes)
  - `/cliente/reservas-pendientes` - Lista de reservas activas
  - `/cliente/historico` - Histórico de reservas
  - `/cliente/mis-datos` - Gestión de datos personales
  - `/cliente/nueva-reserva` - Formulario de nueva reserva
  - `/cliente/cancelar-reserva/{id}` - Endpoint para cancelar reservas

#### DTOs
- **ActualizarDatosClienteDTO.java**: Validación de datos del cliente
  - Validaciones de nombre, email y teléfono
  - Soporte para cambio de contraseña opcional

#### Seguridad
- Configuración actualizada en `SecurityConfig.java`:
  - Rutas `/cliente/**` requieren rol `CLIENTE` o `WEBMASTER`
  - Redireccionamiento automático según rol en `CustomAuthenticationSuccessHandler.java`

### Frontend

#### Diseño
- **Estilo coherente**: Mantiene la paleta de colores existente
  - Primary: #C17C64 (Terracota)
  - Secondary: #1A3C34 (Verde oscuro)
  - Tipografías: Crimson Pro y Raleway

#### Características UI
- Header con logo y navegación
- Menú de navegación con pestañas activas
- Cards con gradientes y sombras
- Estados visuales para reservas (confirmada, pendiente, cancelada, completada)
- Formularios con validación en tiempo real
- Modales de confirmación
- Mensajes de éxito/error
- Diseño responsive
- Footer consistente

#### JavaScript
- Validación de fechas (fecha fin > fecha inicio)
- Establecer fecha mínima como hoy
- Modal de confirmación para cancelaciones
- Validación de contraseñas coincidentes

## 🚀 Uso

### Acceso
1. Iniciar sesión con credenciales de cliente
2. Automáticamente redirigido a `/cliente/reservas-pendientes`
3. Navegar entre las diferentes secciones usando el menú

### Usuarios de Prueba
Según `data.sql`:
- **Email**: juan@gmail.com
- **Email**: maria@gmail.com
- **Contraseña**: password (para ambos)

### Funcionalidades

#### Ver Reservas
- Las reservas activas son aquellas pendientes o confirmadas con fecha futura
- El histórico incluye reservas completadas, canceladas y confirmadas pasadas

#### Hacer una Reserva
1. Seleccionar habitación del listado disponible
2. Elegir fechas de entrada y salida
3. Indicar número de personas
4. (Opcional) Seleccionar modelo de reserva con comidas
5. (Opcional) Añadir notas especiales
6. Confirmar reserva

#### Cancelar una Reserva
1. Ir a "Reservas Activas"
2. Hacer clic en "Cancelar Reserva"
3. Confirmar en el modal
4. La reserva pasa al histórico con estado "cancelada"

#### Actualizar Datos
1. Ir a "Mis Datos"
2. Modificar campos deseados
3. (Opcional) Cambiar contraseña
4. Guardar cambios

## 📱 Responsive

Todas las páginas son responsive y se adaptan a diferentes tamaños de pantalla:
- Desktop: Vista completa con grids
- Tablet: Grids adaptados
- Mobile: Vista en columna única

## 🔒 Seguridad

- Todas las operaciones verifican que el usuario sea el propietario de la reserva
- Las contraseñas se encriptan con BCrypt
- Validación de datos en frontend y backend
- Protección CSRF habilitada
- Sesiones seguras

## 📝 Notas Técnicas

- Las páginas utilizan Thymeleaf para renderizado del lado del servidor
- Spring Security gestiona la autenticación y autorización
- JPA/Hibernate para persistencia de datos
- Validación con Jakarta Bean Validation
- CSS embebido en las plantillas para facilitar personalización

## 🎯 Mejoras Futuras Posibles

- Añadir paginación en listas de reservas
- Implementar búsqueda y filtrado de reservas
- Añadir descarga de PDF con detalles de reserva
- Implementar sistema de valoraciones post-estancia
- Añadir calendario visual para selección de fechas
- Notificaciones por email de cambios en reservas
- Sistema de pagos integrado
