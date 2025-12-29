package com.villadictos.app.controller;

import com.villadictos.app.dto.BloqueoDTO;
import com.villadictos.app.dto.CrearClienteDTO;
import com.villadictos.app.dto.CrearReservaDTO;
import com.villadictos.app.dto.DashboardStatsDTO;
import com.villadictos.app.dto.HabitacionEstadoDTO;
import com.villadictos.app.model.Bloqueo;
import com.villadictos.app.model.Habitacion;
import com.villadictos.app.model.ModeloReserva;
import com.villadictos.app.model.Reserva;
import com.villadictos.app.model.Usuario;
import com.villadictos.app.repository.HabitacionRepository;
import com.villadictos.app.repository.ModeloReservaRepository;
import com.villadictos.app.repository.UsuarioRepository;
import com.villadictos.app.service.BloqueoService;
import com.villadictos.app.service.RecepcionService;
import com.villadictos.app.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/recepcion")
public class RecepcionController {

    private final RecepcionService recepcionService;
    private final ReservaService reservaService;
    private final BloqueoService bloqueoService;
    private final HabitacionRepository habitacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModeloReservaRepository modeloReservaRepository;
    private final PasswordEncoder passwordEncoder;

    public RecepcionController(RecepcionService recepcionService,
            ReservaService reservaService,
            BloqueoService bloqueoService,
            HabitacionRepository habitacionRepository,
            UsuarioRepository usuarioRepository,
            ModeloReservaRepository modeloReservaRepository,
            PasswordEncoder passwordEncoder) {
        this.recepcionService = recepcionService;
        this.reservaService = reservaService;
        this.bloqueoService = bloqueoService;
        this.habitacionRepository = habitacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.modeloReservaRepository = modeloReservaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================== DASHBOARD ====================
    @GetMapping
    public String dashboard(Model model) {
        DashboardStatsDTO stats = recepcionService.obtenerEstadisticas();
        List<Reserva> checkInsHoy = reservaService.findCheckInsHoy();
        List<Reserva> checkOutsHoy = reservaService.findCheckOutsHoy();

        model.addAttribute("stats", stats);
        model.addAttribute("checkInsHoy", checkInsHoy);
        model.addAttribute("checkOutsHoy", checkOutsHoy);
        return "recepcion/dashboard";
    }

    // ==================== HABITACIONES ====================
    @GetMapping("/habitaciones")
    public String habitaciones(Model model) {
        List<HabitacionEstadoDTO> habitaciones = recepcionService.obtenerEstadoHabitaciones();
        model.addAttribute("habitaciones", habitaciones);
        return "recepcion/habitaciones";
    }

    // ==================== RESERVAS ====================
    @GetMapping("/reservas")
    public String listaReservas(Model model) {
        List<Reserva> reservas = reservaService.findAll();
        model.addAttribute("reservas", reservas);
        return "recepcion/reservas/lista";
    }

    @GetMapping("/reservas/nueva")
    public String nuevaReservaForm(Model model) {
        model.addAttribute("reservaDTO", new CrearReservaDTO());
        model.addAttribute("habitaciones", habitacionRepository.findAll());
        model.addAttribute("clientes", recepcionService.obtenerClientes());
        model.addAttribute("modelosReserva", modeloReservaRepository.findAll());
        return "recepcion/reservas/nueva";
    }

    @PostMapping("/reservas/nueva")
    public String crearReserva(@Valid @ModelAttribute("reservaDTO") CrearReservaDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("habitaciones", habitacionRepository.findAll());
            model.addAttribute("clientes", recepcionService.obtenerClientes());
            model.addAttribute("modelosReserva", modeloReservaRepository.findAll());
            return "recepcion/reservas/nueva";
        }

        try {
            reservaService.crearReserva(dto);
            redirectAttributes.addFlashAttribute("success", "Reserva creada correctamente");
            return "redirect:/recepcion/reservas";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("habitaciones", habitacionRepository.findAll());
            model.addAttribute("clientes", recepcionService.obtenerClientes());
            model.addAttribute("modelosReserva", modeloReservaRepository.findAll());
            return "recepcion/reservas/nueva";
        }
    }

    // ==================== BLOQUEOS ====================
    @GetMapping("/bloqueos")
    public String listaBloqueos(Model model) {
        List<Bloqueo> bloqueos = bloqueoService.findBloqueosActivos();
        model.addAttribute("bloqueos", bloqueos);
        return "recepcion/bloqueos/lista";
    }

    @GetMapping("/bloqueos/nuevo")
    public String nuevoBloqueoForm(Model model) {
        model.addAttribute("bloqueoDTO", new BloqueoDTO());
        model.addAttribute("habitaciones", habitacionRepository.findAll());
        return "recepcion/bloqueos/nuevo";
    }

    @PostMapping("/bloqueos/nuevo")
    public String crearBloqueo(@Valid @ModelAttribute("bloqueoDTO") BloqueoDTO dto,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("habitaciones", habitacionRepository.findAll());
            return "recepcion/bloqueos/nuevo";
        }

        try {
            Usuario creador = usuarioRepository.findByEmail(userDetails.getUsername()).orElse(null);
            bloqueoService.crearBloqueo(dto, creador);
            redirectAttributes.addFlashAttribute("success", "Bloqueo creado correctamente");
            return "redirect:/recepcion/bloqueos";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("habitaciones", habitacionRepository.findAll());
            return "recepcion/bloqueos/nuevo";
        }
    }

    @PostMapping("/bloqueos/{id}/eliminar")
    public String eliminarBloqueo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bloqueoService.eliminarBloqueo(id);
        redirectAttributes.addFlashAttribute("success", "Bloqueo eliminado correctamente");
        return "redirect:/recepcion/bloqueos";
    }

    // ==================== CLIENTES ====================
    @GetMapping("/clientes")
    public String listaClientes(Model model) {
        List<Usuario> clientes = recepcionService.obtenerClientes();
        model.addAttribute("clientes", clientes);
        return "recepcion/clientes";
    }

    @PostMapping("/clientes/nuevo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> crearCliente(@RequestBody CrearClienteDTO dto) {
        Map<String, Object> response = new HashMap<>();

        // Validar email único
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            response.put("success", false);
            response.put("error", "Ya existe un cliente con ese email");
            return ResponseEntity.badRequest().body(response);
        }

        // Crear cliente
        Usuario cliente = new Usuario();
        cliente.setNombre(dto.getNombre());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setRol(Usuario.Rol.cliente);
        // Contraseña temporal (el cliente la cambiará después)
        cliente.setPasswordHash(passwordEncoder.encode("temporal123"));

        Usuario saved = usuarioRepository.save(cliente);

        response.put("success", true);
        response.put("id", saved.getId());
        response.put("nombre", saved.getNombre());
        response.put("email", saved.getEmail());

        return ResponseEntity.ok(response);
    }
}
