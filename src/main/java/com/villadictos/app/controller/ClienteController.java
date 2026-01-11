package com.villadictos.app.controller;

import com.villadictos.app.dto.ActualizarDatosClienteDTO;
import com.villadictos.app.dto.CrearReservaDTO;
import com.villadictos.app.model.Habitacion;
import com.villadictos.app.model.ModeloReserva;
import com.villadictos.app.model.Reserva;
import com.villadictos.app.model.Usuario;
import com.villadictos.app.repository.HabitacionRepository;
import com.villadictos.app.repository.ModeloReservaRepository;
import com.villadictos.app.repository.ReservaRepository;
import com.villadictos.app.repository.UsuarioRepository;
import com.villadictos.app.service.ReservaService;
import com.villadictos.app.service.RoomService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

    public ClienteController(ReservaRepository reservaRepository,
            UsuarioRepository usuarioRepository,
            HabitacionRepository habitacionRepository,
            ModeloReservaRepository modeloReservaRepository,
            ReservaService reservaService,
            RoomService roomService,
            PasswordEncoder passwordEncoder) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.habitacionRepository = habitacionRepository;
        this.modeloReservaRepository = modeloReservaRepository;
        this.reservaService = reservaService;
        this.roomService = roomService;
        this.passwordEncoder = passwordEncoder;
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
                redirectAttributes.addFlashAttribute("success", "Datos actualizados correctamente. Por favor, inicia sesión nuevamente.");
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
            @RequestParam(required = false) Long habitacionId,
            Model model) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        CrearReservaDTO reservaDTO = new CrearReservaDTO();
        reservaDTO.setIdCliente(usuario.getId());

        if (habitacionId != null) {
            reservaDTO.setIdHabitacion(habitacionId);
        }

        model.addAttribute("reservaDTO", reservaDTO);
        model.addAttribute("habitaciones", habitacionRepository.findAll());
        model.addAttribute("modelosReserva", modeloReservaRepository.findAll());
        model.addAttribute("usuario", usuario);
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
            model.addAttribute("habitaciones", habitacionRepository.findAll());
            model.addAttribute("modelosReserva", modeloReservaRepository.findAll());
            model.addAttribute("usuario", usuario);
            return "cliente/nueva-reserva";
        }

        try {
            Reserva reserva = reservaService.crearReserva(dto);
            redirectAttributes.addFlashAttribute("success",
                    "Reserva creada correctamente. Número de reserva: " + reserva.getId());
            return "redirect:/cliente/reservas-pendientes";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("habitaciones", habitacionRepository.findAll());
            model.addAttribute("modelosReserva", modeloReservaRepository.findAll());
            model.addAttribute("usuario", usuario);
            return "cliente/nueva-reserva";
        }
    }

    /**
     * Cancelar una reserva
     */
    @PostMapping("/cancelar-reserva/{id}")
    public String cancelarReserva(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
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
            redirectAttributes.addFlashAttribute("error", "No se puede cancelar esta reserva");
            return "redirect:/cliente/reservas-pendientes";
        }

        reserva.setEstado(Reserva.EstadoReserva.cancelada);
        reservaRepository.save(reserva);

        redirectAttributes.addFlashAttribute("success", "Reserva cancelada correctamente");
        return "redirect:/cliente/historico";
    }
}
