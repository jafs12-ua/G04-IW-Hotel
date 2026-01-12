package com.villadictos.app.controller;

import com.villadictos.app.dto.ActualizarDatosClienteDTO;
import com.villadictos.app.dto.CrearReservaDTO;
import com.villadictos.app.dto.ReservarServicioDTO;
import com.villadictos.app.model.Habitacion;
import com.villadictos.app.model.ModeloReserva;
import com.villadictos.app.model.Reserva;
import com.villadictos.app.model.ReservaServicio;
import com.villadictos.app.model.Usuario;
import com.villadictos.app.repository.HabitacionRepository;
import com.villadictos.app.repository.ModeloReservaRepository;
import com.villadictos.app.repository.ReservaRepository;
import com.villadictos.app.repository.SalaRepository;
import com.villadictos.app.repository.ServicioRepository;
import com.villadictos.app.repository.TipoHabitacionRepository;
import com.villadictos.app.repository.UsuarioRepository;
import com.villadictos.app.service.ReservaService;
import com.villadictos.app.service.ReservaServicioService;
import com.villadictos.app.service.RoomService;
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

import java.time.LocalDate;
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
            SalaRepository salaRepository) {
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
     * Crear nueva reserva
     */
    @PostMapping("/nueva-reserva")
    public String crearReserva(@Valid @ModelAttribute("reservaDTO") CrearReservaDTO dto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
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
            Reserva reserva = reservaService.crearReserva(dto);
            String mensaje;
            if (reserva.getSala() != null) {
                mensaje = "Reserva de sala creada correctamente. Número de reserva: " + reserva.getId() +
                        ". Sala: " + reserva.getSala().getNombre();
            } else {
                mensaje = "Reserva creada correctamente. Número de reserva: " + reserva.getId() +
                        ". Se te ha asignado la habitación " + reserva.getHabitacion().getNumeroHabitacion();
            }
            redirectAttributes.addFlashAttribute("success", mensaje);
            return "redirect:/cliente/reservas-pendientes";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tiposHabitacion", tipoHabitacionRepository.findAll());
            model.addAttribute("salas", salaRepository.findAll());
            model.addAttribute("modelosReserva", modeloReservaRepository.findAll());
            model.addAttribute("usuario", usuario);
            return "cliente/nueva-reserva";
        }
    }

    /**
     * Cancelar una reserva
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

            reserva.setEstado(Reserva.EstadoReserva.cancelada);
            reservaRepository.save(reserva);

            redirectAttributes.addFlashAttribute("success", "Reserva #" + id + " cancelada correctamente");

            // Si la reserva era de hoy o futuro inmediato, quizás liberar habitación sea
            // necesario (depende de lógica negocio)
            // Por ahora solo cambiamos estado.

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
        model.addAttribute("usuario", usuario);
        return "cliente/reservar-servicios";
    }

    @PostMapping("/reservar-servicios")
    public String procesarReservaServicio(@Valid @ModelAttribute("reservarDTO") ReservarServicioDTO dto,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
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
            ReservaServicio reservaServicio = reservaServicioService.reservarServicio(dto);
            redirectAttributes.addFlashAttribute("success",
                    "Servicio agregado correctamente a tu reserva");
            return "redirect:/cliente/reservas-pendientes";
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
        }
    }
}
