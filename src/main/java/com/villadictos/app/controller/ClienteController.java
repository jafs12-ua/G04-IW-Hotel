package com.villadictos.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.villadictos.app.dto.ActualizarDatosClienteDTO;
import com.villadictos.app.dto.CrearReservaDTO;
import com.villadictos.app.dto.ReservarServicioDTO;
import com.villadictos.app.dto.ReservarMultiplesServiciosDTO;
import com.villadictos.app.dto.ServicioItemDTO;
import com.villadictos.app.model.*;
import com.villadictos.app.repository.*;
import com.villadictos.app.service.ReservaService;
import com.villadictos.app.service.ReservaServicioService;
import com.villadictos.app.service.RoomService;
import com.villadictos.app.service.TpvService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HabitacionRepository habitacionRepository;
    private final ModeloReservaRepository modeloReservaRepository;
    private final ReservaService reservaService;
    private final RoomService roomService;
    private final PasswordEncoder passwordEncoder;
    private final ServicioRepository servicioRepository;
    private final ReservaServicioService reservaServicioService;
    private final TipoHabitacionRepository tipoHabitacionRepository;
    private final SalaRepository salaRepository;
    private final TpvService tpvService;
    private final PendingPaymentRepository pendingPaymentRepository;
    private final TemporadaRepository temporadaRepository;
    private final ObjectMapper objectMapper;

    public ClienteController(ReservaRepository reservaRepository,
            UsuarioRepository usuarioRepository,
            HabitacionRepository habitacionRepository,
            ModeloReservaRepository modeloReservaRepository,
            ReservaService reservaService,
            RoomService roomService,
            PasswordEncoder passwordEncoder,
            ServicioRepository servicioRepository,
            ReservaServicioService reservaServicioService,
            TipoHabitacionRepository tipoHabitacionRepository,
            SalaRepository salaRepository,
            TpvService tpvService,
            PendingPaymentRepository pendingPaymentRepository,
            TemporadaRepository temporadaRepository) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.habitacionRepository = habitacionRepository;
        this.modeloReservaRepository = modeloReservaRepository;
        this.reservaService = reservaService;
        this.roomService = roomService;
        this.passwordEncoder = passwordEncoder;
        this.servicioRepository = servicioRepository;
        this.reservaServicioService = reservaServicioService;
        this.tipoHabitacionRepository = tipoHabitacionRepository;
        this.salaRepository = salaRepository;
        this.tpvService = tpvService;
        this.pendingPaymentRepository = pendingPaymentRepository;
        this.temporadaRepository = temporadaRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // For LocalDate serialization
    }

    /**
     * Dashboard del cliente - Redirecciona a reservas pendientes
     */
    @GetMapping
    public String dashboard() {
        return "redirect:/cliente/reservas-pendientes";
    }

    /**
     * Página de reservas pendientes
     */
    @GetMapping("/reservas-pendientes")
    public String reservasPendientes(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Reserva> reservasPendientes;
        try {
            LocalDate hoy = LocalDate.now();
            reservasPendientes = reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                    .stream()
                    .filter(r -> r.getEstado() != null &&
                            (r.getEstado() == Reserva.EstadoReserva.pendiente ||
                                    (r.getEstado() == Reserva.EstadoReserva.confirmada &&
                                            r.getFechaInicio() != null && !r.getFechaInicio().isBefore(hoy))))
                    .collect(Collectors.toList());

            // Cargar servicios para cada reserva
            for (Reserva reserva : reservasPendientes) {
                List<ReservaServicio> servicios = reservaServicioService.findByReservaId(reserva.getId());
                reserva.setReservaServicios(servicios);
            }
        } catch (Exception e) {
            reservasPendientes = List.of();
        }

        model.addAttribute("reservas", reservasPendientes);
        model.addAttribute("usuario", usuario);
        return "cliente/reservas-pendientes";
    }

    /**
     * Página de histórico de reservas
     */
    @GetMapping("/historico")
    public String historico(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Reserva> reservasCompletadas;
        try {
            LocalDate hoy = LocalDate.now();
            reservasCompletadas = reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                    .stream()
                    .filter(r -> r.getEstado() != null &&
                            (r.getEstado() == Reserva.EstadoReserva.completada ||
                                    r.getEstado() == Reserva.EstadoReserva.cancelada ||
                                    (r.getEstado() == Reserva.EstadoReserva.confirmada &&
                                            r.getFechaFin() != null && r.getFechaFin().isBefore(hoy))))
                    .collect(Collectors.toList());

            // Cargar servicios para cada reserva
            for (Reserva reserva : reservasCompletadas) {
                List<ReservaServicio> servicios = reservaServicioService.findByReservaId(reserva.getId());
                reserva.setReservaServicios(servicios);
            }
        } catch (Exception e) {
            reservasCompletadas = List.of();
        }

        model.addAttribute("reservas", reservasCompletadas);
        model.addAttribute("usuario", usuario);
        return "cliente/historico";
    }

    /**
     * Página de mis datos
     */
    @GetMapping("/mis-datos")
    public String misDatos(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ActualizarDatosClienteDTO dto = new ActualizarDatosClienteDTO();
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setTelefono(usuario.getTelefono());

        model.addAttribute("datosDTO", dto);
        model.addAttribute("usuario", usuario);
        return "cliente/mis-datos";
    }

    /**
     * Actualizar datos del cliente
     */
    @PostMapping("/mis-datos")
    public String actualizarDatos(@Valid @ModelAttribute("datosDTO") ActualizarDatosClienteDTO dto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (result.hasErrors()) {
                model.addAttribute("datosDTO", dto);
                model.addAttribute("usuario", usuario);
                return "cliente/mis-datos";
            }

            // Validar contraseña si se proporciona
            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                if (dto.getPassword().length() < 6) {
                    model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres");
                    model.addAttribute("datosDTO", dto);
                    model.addAttribute("usuario", usuario);
                    return "cliente/mis-datos";
                }
                if (dto.getConfirmPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
                    model.addAttribute("error", "Las contraseñas no coinciden");
                    model.addAttribute("datosDTO", dto);
                    model.addAttribute("usuario", usuario);
                    return "cliente/mis-datos";
                }
            }

            boolean emailCambiado = false;
            // Verificar si el email ya existe (si es diferente al actual)
            if (!usuario.getEmail().equals(dto.getEmail())) {
                if (usuarioRepository.existsByEmail(dto.getEmail())) {
                    model.addAttribute("error", "El email ya está en uso");
                    model.addAttribute("datosDTO", dto);
                    model.addAttribute("usuario", usuario);
                    return "cliente/mis-datos";
                }
                usuario.setEmail(dto.getEmail());
                emailCambiado = true;
            }

            usuario.setNombre(dto.getNombre());
            usuario.setTelefono(dto.getTelefono());

            // Actualizar contraseña si se proporciona
            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
            }

            usuarioRepository.save(usuario);

            // Si se cambió el email o la contraseña, cerrar sesión y pedir login nuevamente
            if (emailCambiado || (dto.getPassword() != null && !dto.getPassword().isEmpty())) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) {
                    new SecurityContextLogoutHandler().logout(request, response, auth);
                }
                redirectAttributes.addFlashAttribute("success",
                        "Datos actualizados correctamente. Por favor, inicia sesión nuevamente.");
                return "redirect:/login";
            }

            redirectAttributes.addFlashAttribute("success", "Datos actualizados correctamente");
            return "redirect:/cliente/mis-datos";
        } catch (Exception e) {
            e.printStackTrace(); // Esto imprimirá el error en la consola
            redirectAttributes.addFlashAttribute("error", "Error al actualizar los datos: " + e.getMessage());
            return "redirect:/cliente/mis-datos";
        }
    }

    /**
     * Página para hacer una nueva reserva
     */
    @GetMapping("/nueva-reserva")
    public String nuevaReservaForm(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long tipoId,
            @RequestParam(required = false) Long salaId,
            @RequestParam(required = false) Integer modeloIndex,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Integer cantidadAdultos,
            Model model) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        CrearReservaDTO reservaDTO = new CrearReservaDTO();
        reservaDTO.setIdCliente(usuario.getId());

        // Si viene tipoId desde parámetros, preseleccionarlo (habitación)
        if (tipoId != null) {
            reservaDTO.setIdTipoHabitacion(tipoId);
        }

        // Si vienen fechas desde parámetros, preseleccionarlas
        if (fechaInicio != null) {
            reservaDTO.setFechaInicio(fechaInicio);
        }
        if (fechaFin != null) {
            reservaDTO.setFechaFin(fechaFin);
        }

        // Si viene cantidad de adultos desde parámetros, preseleccionarla
        if (cantidadAdultos != null) {
            reservaDTO.setNumPersonas(cantidadAdultos);
        }

        // Si viene salaId desde parámetros, preseleccionarlo y cambiar tipo a SALA
        if (salaId != null) {
            reservaDTO.setIdSala(salaId);
            reservaDTO.setTipoReserva(CrearReservaDTO.TipoReserva.SALA);
        }

        model.addAttribute("reservaDTO", reservaDTO);
        model.addAttribute("tiposHabitacion", tipoHabitacionRepository.findAll());
        model.addAttribute("salas", salaRepository.findAll());
        model.addAttribute("modelosReserva", modeloReservaRepository.findAll());
        model.addAttribute("usuario", usuario);
        model.addAttribute("modeloPreseleccionado", modeloIndex);
        model.addAttribute("salaPreseleccionada", salaId);
        return "cliente/nueva-reserva";
    }

    /**
     * Crear nueva reserva - Now redirects to TPV for payment
     */
    @PostMapping("/nueva-reserva")
    public String crearReserva(@Valid @ModelAttribute("reservaDTO") CrearReservaDTO dto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        dto.setIdCliente(usuario.getId());

        if (result.hasErrors()) {
            model.addAttribute("tiposHabitacion", tipoHabitacionRepository.findAll());
            model.addAttribute("salas", salaRepository.findAll());
            model.addAttribute("modelosReserva", modeloReservaRepository.findAll());
            model.addAttribute("usuario", usuario);
            return "cliente/nueva-reserva";
        }

        try {
            // Validate the reservation data before payment
            validateReservaData(dto);

            // Calculate total price
            BigDecimal precioTotal = calcularPrecioReserva(dto);

            // Serialize DTO to JSON
            String paymentData = objectMapper.writeValueAsString(dto);

            // Create pending payment
            PendingPayment pendingPayment = new PendingPayment();
            pendingPayment.setUsuario(usuario);
            pendingPayment.setPaymentType(PendingPayment.PaymentType.RESERVA);
            pendingPayment.setPaymentData(paymentData);
            pendingPayment.setAmount(precioTotal);
            pendingPayment.setStatus(PendingPayment.PaymentStatus.PENDING);

            // Build callback URL
            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                baseUrl += ":" + request.getServerPort();
            }
            String callbackUrl = baseUrl + "/tpv/callback";

            // Init payment with TPV
            TpvService.PaymentInitResponse tpvResponse = tpvService.initPayment(
                    precioTotal,
                    callbackUrl,
                    "RESERVA-" + usuario.getId() + "-" + System.currentTimeMillis());

            // Save pending payment with token
            pendingPayment.setToken(tpvResponse.getToken());
            pendingPaymentRepository.save(pendingPayment);

            // Redirect to TPV payment page
            return "redirect:" + tpvResponse.getPaymentUrl();

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tiposHabitacion", tipoHabitacionRepository.findAll());
            model.addAttribute("salas", salaRepository.findAll());
            model.addAttribute("modelosReserva", modeloReservaRepository.findAll());
            model.addAttribute("usuario", usuario);
            return "cliente/nueva-reserva";
        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar el pago: " + e.getMessage());
            model.addAttribute("tiposHabitacion", tipoHabitacionRepository.findAll());
            model.addAttribute("salas", salaRepository.findAll());
            model.addAttribute("modelosReserva", modeloReservaRepository.findAll());
            model.addAttribute("usuario", usuario);
            return "cliente/nueva-reserva";
        }
    }

    /**
     * Validate reservation data before payment
     */
    private void validateReservaData(CrearReservaDTO dto) {
        // Validate dates
        if (dto.getFechaFin().isBefore(dto.getFechaInicio()) || dto.getFechaFin().isEqual(dto.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio");
        }

        if (dto.getTipoReserva() == CrearReservaDTO.TipoReserva.SALA) {
            if (dto.getIdSala() == null) {
                throw new IllegalArgumentException("Debe seleccionar una sala");
            }
            Sala sala = salaRepository.findById(dto.getIdSala())
                    .orElseThrow(() -> new IllegalArgumentException("Sala no encontrada"));
            if (dto.getNumPersonas() > sala.getAforoMax()) {
                throw new IllegalArgumentException("La sala no tiene capacidad suficiente");
            }
            // Check availability
            List<Reserva> reservasExistentes = reservaRepository.findBySalaIdAndFechaRange(
                    dto.getIdSala(), dto.getFechaInicio(), dto.getFechaFin());
            if (!reservasExistentes.isEmpty()) {
                throw new IllegalArgumentException("La sala no está disponible para las fechas seleccionadas");
            }
        } else {
            if (dto.getIdTipoHabitacion() == null) {
                throw new IllegalArgumentException("Debe seleccionar un tipo de habitación");
            }
            TipoHabitacion tipoHabitacion = tipoHabitacionRepository.findById(dto.getIdTipoHabitacion())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de habitación no encontrado"));
            if (dto.getNumPersonas() > tipoHabitacion.getCapacidadPersonas()) {
                throw new IllegalArgumentException("El tipo de habitación no tiene capacidad suficiente");
            }
            // Check availability
            List<Habitacion> habitacionesDisponibles = habitacionRepository.findAvailableByTypeAndDateRange(
                    dto.getIdTipoHabitacion(), dto.getFechaInicio(), dto.getFechaFin());
            if (habitacionesDisponibles.isEmpty()) {
                throw new IllegalArgumentException(
                        "No hay habitaciones disponibles del tipo seleccionado para las fechas indicadas");
            }
        }
    }

    /**
     * Calculate reservation price without creating it
     */
    private BigDecimal calcularPrecioReserva(CrearReservaDTO dto) {
        long dias = ChronoUnit.DAYS.between(dto.getFechaInicio(), dto.getFechaFin());

        if (dto.getTipoReserva() == CrearReservaDTO.TipoReserva.SALA) {
            Sala sala = salaRepository.findById(dto.getIdSala())
                    .orElseThrow(() -> new IllegalArgumentException("Sala no encontrada"));
            return sala.getPrecioBaseDia().multiply(BigDecimal.valueOf(dias));
        } else {
            TipoHabitacion tipoHabitacion = tipoHabitacionRepository.findById(dto.getIdTipoHabitacion())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de habitación no encontrado"));

            BigDecimal precioBase = tipoHabitacion.getPrecioBaseNoche();

            // Get season factor
            Temporada temporada = temporadaRepository.findActiveSeasonByDate(dto.getFechaInicio()).orElse(null);
            BigDecimal factorTemporada = temporada != null ? temporada.getFactorPrecio() : BigDecimal.ONE;

            // Get model additional price
            BigDecimal precioAdicional = BigDecimal.ZERO;
            if (dto.getIdModeloReserva() != null) {
                ModeloReserva modeloReserva = modeloReservaRepository.findById(dto.getIdModeloReserva()).orElse(null);
                if (modeloReserva != null) {
                    precioAdicional = modeloReserva.getPrecioAdicionalNoche();
                }
            }

            BigDecimal precioNoche = precioBase.multiply(factorTemporada).add(precioAdicional);
            return precioNoche.multiply(BigDecimal.valueOf(dias));
        }
    }

    /**
     * Cancelar una reserva - Procesa reembolso del pago
     */
    @PostMapping("/cancelar-reserva/{id}")
    @org.springframework.transaction.annotation.Transactional
    public String cancelarReserva(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Reserva reserva = reservaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            // Verificar que la reserva pertenece al usuario
            if (!reserva.getUsuario().getId().equals(usuario.getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para cancelar esta reserva");
                return "redirect:/cliente/reservas-pendientes";
            }

            // Solo se pueden cancelar reservas pendientes o confirmadas
            if (reserva.getEstado() != Reserva.EstadoReserva.pendiente &&
                    reserva.getEstado() != Reserva.EstadoReserva.confirmada) {
                redirectAttributes.addFlashAttribute("error",
                        "No se puede cancelar esta reserva porque no está activa");
                return "redirect:/cliente/reservas-pendientes";
            }

            // Process refunds for all transactions (reservation + services)
            BigDecimal totalReembolso = BigDecimal.ZERO;
            int transaccionesExitosas = 0;
            int transaccionesFallidas = 0;
            StringBuilder errores = new StringBuilder();

            // 1. Refund reservation if it has a TPV token
            if (reserva.getTpvToken() != null && !reserva.getTpvToken().isEmpty()) {
                try {
                    BigDecimal precioReserva = reserva.getPrecioTotal();
                    tpvService.processRefund(reserva.getTpvToken(), precioReserva);
                    totalReembolso = totalReembolso.add(precioReserva);
                    transaccionesExitosas++;
                    System.out.println("Refund reserva: " + precioReserva + "€");
                } catch (Exception e) {
                    transaccionesFallidas++;
                    errores.append("Reserva: ").append(e.getMessage()).append("; ");
                    System.err.println("Error refund reserva: " + e.getMessage());
                }
            }

            // 2. Refund each service that has its own TPV token
            List<ReservaServicio> servicios = reservaServicioService.findByReservaId(reserva.getId());
            for (ReservaServicio rs : servicios) {
                if (rs.getTpvToken() != null && !rs.getTpvToken().isEmpty()) {
                    try {
                        BigDecimal precioServicio = rs.getSubtotal();
                        tpvService.processRefund(rs.getTpvToken(), precioServicio);
                        totalReembolso = totalReembolso.add(precioServicio);
                        transaccionesExitosas++;
                        System.out.println(
                                "Refund servicio " + rs.getServicio().getNombre() + ": " + precioServicio + "€");
                    } catch (Exception e) {
                        transaccionesFallidas++;
                        errores.append(rs.getServicio().getNombre()).append(": ").append(e.getMessage()).append("; ");
                        System.err.println("Error refund servicio: " + e.getMessage());
                    }
                }
            }

            // Update reservation status
            reserva.setEstado(Reserva.EstadoReserva.cancelada);
            reservaRepository.save(reserva);

            // Build success message
            StringBuilder mensaje = new StringBuilder("Reserva #" + id + " cancelada correctamente.");
            if (transaccionesExitosas > 0) {
                mensaje.append(" Se han procesado ").append(transaccionesExitosas)
                        .append(" reembolso(s) por un total de ").append(totalReembolso).append("€.");
            }
            if (transaccionesFallidas > 0) {
                mensaje.append(" Nota: ").append(transaccionesFallidas)
                        .append(" reembolso(s) fallido(s). Por favor, contacta con recepción.");
            }

            redirectAttributes.addFlashAttribute("success", mensaje.toString());

            return "redirect:/cliente/historico";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al cancelar la reserva: " + e.getMessage());
            return "redirect:/cliente/reservas-pendientes";
        }
    }

    @GetMapping("/reservar-servicios")
    public String reservarServicios(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtener reservas activas del usuario
        List<Reserva> reservasActivas = reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                .stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada ||
                        r.getEstado() == Reserva.EstadoReserva.pendiente)
                .collect(Collectors.toList());

        model.addAttribute("servicios", servicioRepository.findAll());
        model.addAttribute("reservas", reservasActivas);
        model.addAttribute("reservarDTO", new ReservarServicioDTO());
        model.addAttribute("reservarMultiplesDTO", new ReservarMultiplesServiciosDTO());
        model.addAttribute("usuario", usuario);
        return "cliente/reservar-servicios";
    }

    @PostMapping("/reservar-servicios")
    public String procesarReservaServicio(@Valid @ModelAttribute("reservarDTO") ReservarServicioDTO dto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (result.hasErrors()) {
            List<Reserva> reservasActivas = reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                    .stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada ||
                            r.getEstado() == Reserva.EstadoReserva.pendiente)
                    .collect(Collectors.toList());

            model.addAttribute("servicios", servicioRepository.findAll());
            model.addAttribute("reservas", reservasActivas);
            model.addAttribute("usuario", usuario);
            return "cliente/reservar-servicios";
        }

        try {
            // Get service and calculate price
            Servicio servicio = servicioRepository.findById(dto.getIdServicio())
                    .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

            BigDecimal precioTotal = servicio.getPrecio().multiply(BigDecimal.valueOf(dto.getCantidad()));

            // Serialize DTO to JSON
            String paymentData = objectMapper.writeValueAsString(dto);

            // Create pending payment
            PendingPayment pendingPayment = new PendingPayment();
            pendingPayment.setUsuario(usuario);
            pendingPayment.setPaymentType(PendingPayment.PaymentType.SERVICIO);
            pendingPayment.setPaymentData(paymentData);
            pendingPayment.setAmount(precioTotal);
            pendingPayment.setStatus(PendingPayment.PaymentStatus.PENDING);

            // Build callback URL
            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                baseUrl += ":" + request.getServerPort();
            }
            String callbackUrl = baseUrl + "/tpv/callback";

            // Init payment with TPV
            TpvService.PaymentInitResponse tpvResponse = tpvService.initPayment(
                    precioTotal,
                    callbackUrl,
                    "SERVICIO-" + usuario.getId() + "-" + System.currentTimeMillis());

            // Save pending payment with token
            pendingPayment.setToken(tpvResponse.getToken());
            pendingPaymentRepository.save(pendingPayment);

            // Redirect to TPV payment page
            return "redirect:" + tpvResponse.getPaymentUrl();

        } catch (IllegalArgumentException e) {
            List<Reserva> reservasActivas = reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                    .stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada ||
                            r.getEstado() == Reserva.EstadoReserva.pendiente)
                    .collect(Collectors.toList());

            model.addAttribute("error", e.getMessage());
            model.addAttribute("servicios", servicioRepository.findAll());
            model.addAttribute("reservas", reservasActivas);
            model.addAttribute("usuario", usuario);
            return "cliente/reservar-servicios";
        } catch (Exception e) {
            List<Reserva> reservasActivas = reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                    .stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada ||
                            r.getEstado() == Reserva.EstadoReserva.pendiente)
                    .collect(Collectors.toList());

            model.addAttribute("error", "Error al procesar el pago: " + e.getMessage());
            model.addAttribute("servicios", servicioRepository.findAll());
            model.addAttribute("reservas", reservasActivas);
            model.addAttribute("usuario", usuario);
            return "cliente/reservar-servicios";
        }
    }

    @PostMapping("/reservar-servicios-multiples")
    public String procesarReservaMultiplesServicios(@Valid @ModelAttribute("reservarMultiplesDTO") ReservarMultiplesServiciosDTO dto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (result.hasErrors()) {
            List<Reserva> reservasActivas = reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                    .stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada ||
                            r.getEstado() == Reserva.EstadoReserva.pendiente)
                    .collect(Collectors.toList());

            model.addAttribute("servicios", servicioRepository.findAll());
            model.addAttribute("reservas", reservasActivas);
            model.addAttribute("reservarDTO", new ReservarServicioDTO());
            model.addAttribute("usuario", usuario);
            model.addAttribute("error", "Por favor, verifica los datos ingresados");
            return "cliente/reservar-servicios";
        }

        try {
            // Calculate total price for all services
            BigDecimal precioTotal = BigDecimal.ZERO;
            for (ServicioItemDTO servicioItem : dto.getServicios()) {
                Servicio servicio = servicioRepository.findById(servicioItem.getIdServicio())
                        .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
                
                BigDecimal precioServicio = servicio.getPrecio().multiply(BigDecimal.valueOf(servicioItem.getCantidad()));
                precioTotal = precioTotal.add(precioServicio);
            }

            // Serialize DTO to JSON
            String paymentData = objectMapper.writeValueAsString(dto);

            // Create pending payment
            PendingPayment pendingPayment = new PendingPayment();
            pendingPayment.setUsuario(usuario);
            pendingPayment.setPaymentType(PendingPayment.PaymentType.SERVICIOS);
            pendingPayment.setPaymentData(paymentData);
            pendingPayment.setAmount(precioTotal);
            pendingPayment.setStatus(PendingPayment.PaymentStatus.PENDING);

            // Build callback URL
            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                baseUrl += ":" + request.getServerPort();
            }
            String callbackUrl = baseUrl + "/tpv/callback";

            // Init payment with TPV
            TpvService.PaymentInitResponse tpvResponse = tpvService.initPayment(
                    precioTotal,
                    callbackUrl,
                    "SERVICIOS-" + usuario.getId() + "-" + System.currentTimeMillis());

            // Save pending payment with token
            pendingPayment.setToken(tpvResponse.getToken());
            pendingPaymentRepository.save(pendingPayment);

            // Redirect to TPV payment page
            return "redirect:" + tpvResponse.getPaymentUrl();

        } catch (IllegalArgumentException e) {
            List<Reserva> reservasActivas = reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                    .stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada ||
                            r.getEstado() == Reserva.EstadoReserva.pendiente)
                    .collect(Collectors.toList());

            model.addAttribute("error", e.getMessage());
            model.addAttribute("servicios", servicioRepository.findAll());
            model.addAttribute("reservas", reservasActivas);
            model.addAttribute("reservarDTO", new ReservarServicioDTO());
            model.addAttribute("usuario", usuario);
            return "cliente/reservar-servicios";
        } catch (Exception e) {
            List<Reserva> reservasActivas = reservaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                    .stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada ||
                            r.getEstado() == Reserva.EstadoReserva.pendiente)
                    .collect(Collectors.toList());

            model.addAttribute("error", "Error al procesar el pago: " + e.getMessage());
            model.addAttribute("servicios", servicioRepository.findAll());
            model.addAttribute("reservas", reservasActivas);
            model.addAttribute("reservarDTO", new ReservarServicioDTO());
            model.addAttribute("usuario", usuario);
            return "cliente/reservar-servicios";
        }
    }
}
