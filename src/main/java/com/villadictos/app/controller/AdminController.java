package com.villadictos.app.controller;

import com.villadictos.app.model.*;
import com.villadictos.app.repository.*;
import com.villadictos.app.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final HabitacionRepository habitacionRepository;
    private final TipoHabitacionRepository tipoHabitacionRepository;
    private final SalaRepository salaRepository;

    private final RecepcionService recepcionService;
    private final ReservaService reservaService;
    private final BloqueoService bloqueoService;

    public AdminController(UsuarioRepository usuarioRepository, HabitacionRepository habitacionRepository,
            TipoHabitacionRepository tipoHabitacionRepository, SalaRepository salaRepository,
            RecepcionService recepcionService, ReservaService reservaService,
            BloqueoService bloqueoService) {
        this.usuarioRepository = usuarioRepository;
        this.habitacionRepository = habitacionRepository;
        this.tipoHabitacionRepository = tipoHabitacionRepository;
        this.salaRepository = salaRepository;
        this.recepcionService = recepcionService;
        this.reservaService = reservaService;
        this.bloqueoService = bloqueoService;
    }

    @GetMapping
    public String dashboard(Model model) {
        // KPIs usando RecepcionService
        var stats = recepcionService.obtenerEstadisticas();

        model.addAttribute("totalHabitaciones", stats.getTotalHabitaciones());
        // Total reservas = pendientes + checkins hoy
        // Para mantener consistencia usamos reservaService
        model.addAttribute("totalReservas", reservaService.findAll().size());
        model.addAttribute("totalUsuarios", stats.getTotalClientes());

        // Cálculo de ingresos
        BigDecimal totalIngresos = reservaService.findAll().stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada
                        || r.getEstado() == Reserva.EstadoReserva.completada)
                .map(Reserva::getPrecioTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalIngresos", totalIngresos);

        return "admin/index";
    }

    // --- HABITACIONES ---
    @GetMapping("/habitaciones")
    public String listarHabitaciones(Model model) {
        model.addAttribute("habitaciones", habitacionRepository.findAll());
        return "admin/habitaciones";
    }

    @GetMapping("/habitaciones/nuevo")
    public String formularioHabitacion(Model model) {
        model.addAttribute("habitacion", new Habitacion());
        model.addAttribute("tipos", tipoHabitacionRepository.findAll());
        return "admin/habitacion-form";
    }

    @PostMapping("/habitaciones/guardar")
    public String guardarHabitacion(@ModelAttribute Habitacion habitacion) {
        habitacionRepository.save(habitacion);
        return "redirect:/admin/habitaciones";
    }

    @GetMapping("/habitaciones/editar/{id}")
    public String editarHabitacion(@PathVariable Long id, Model model) {
        model.addAttribute("habitacion", habitacionRepository.findById(id).orElseThrow());
        model.addAttribute("tipos", tipoHabitacionRepository.findAll());
        return "admin/habitacion-form";
    }

    @GetMapping("/habitaciones/eliminar/{id}")
    public String eliminarHabitacion(@PathVariable Long id) {
        habitacionRepository.deleteById(id);
        return "redirect:/admin/habitaciones";
    }

    // --- TIPOS HABITACION ---
    @GetMapping("/tipos-habitacion")
    public String listarTiposHabitacion(Model model) {
        model.addAttribute("tipos", tipoHabitacionRepository.findAll());
        return "admin/tipos-habitacion";
    }

    @GetMapping("/tipos-habitacion/nuevo")
    public String formularioTipoHabitacion(Model model) {
        model.addAttribute("tipo", new TipoHabitacion());
        return "admin/tipo-habitacion-form";
    }

    @PostMapping("/tipos-habitacion/guardar")
    public String guardarTipoHabitacion(@ModelAttribute TipoHabitacion tipo) {
        tipoHabitacionRepository.save(tipo);
        return "redirect:/admin/tipos-habitacion";
    }

    @GetMapping("/tipos-habitacion/editar/{id}")
    public String editarTipoHabitacion(@PathVariable Long id, Model model) {
        model.addAttribute("tipo", tipoHabitacionRepository.findById(id).orElseThrow());
        return "admin/tipo-habitacion-form";
    }

    @GetMapping("/tipos-habitacion/eliminar/{id}")
    public String eliminarTipoHabitacion(@PathVariable Long id) {
        tipoHabitacionRepository.deleteById(id);
        return "redirect:/admin/tipos-habitacion";
    }

    // --- SALAS ---
    @GetMapping("/salas")
    public String listarSalas(Model model) {
        model.addAttribute("salas", salaRepository.findAll());
        return "admin/salas";
    }

    @GetMapping("/salas/nuevo")
    public String formularioSala(Model model) {
        model.addAttribute("sala", new Sala());
        return "admin/sala-form";
    }

    @PostMapping("/salas/guardar")
    public String guardarSala(@ModelAttribute Sala sala) {
        salaRepository.save(sala);
        return "redirect:/admin/salas";
    }

    @GetMapping("/salas/editar/{id}")
    public String editarSala(@PathVariable Long id, Model model) {
        model.addAttribute("sala", salaRepository.findById(id).orElseThrow());
        return "admin/sala-form";
    }

    @GetMapping("/salas/eliminar/{id}")
    public String eliminarSala(@PathVariable Long id) {
        salaRepository.deleteById(id);
        return "redirect:/admin/salas";
    }

    // --- BLOQUEOS ---
    @GetMapping("/bloqueos")
    public String listarBloqueos(Model model) {
        model.addAttribute("bloqueos", bloqueoService.findAll());
        return "admin/bloqueos";
    }

    @GetMapping("/bloqueos/nuevo")
    public String formularioBloqueo(Model model) {
        model.addAttribute("bloqueo", new Bloqueo());
        model.addAttribute("habitaciones", habitacionRepository.findAll());
        model.addAttribute("salas", salaRepository.findAll());
        return "admin/bloqueo-form";
    }

    @PostMapping("/bloqueos/guardar")
    public String guardarBloqueo(@ModelAttribute Bloqueo bloqueo) {
        // Obtener usuario actual
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        bloqueo.setUsuarioCreador(usuario);

        // Guardar usando el servicio
        bloqueoService.save(bloqueo);

        return "redirect:/admin/bloqueos";
    }

    @GetMapping("/bloqueos/editar/{id}")
    public String editarBloqueo(@PathVariable Long id, Model model) {
        Bloqueo bloqueo = bloqueoService.findById(id);
        if (bloqueo == null) {
            throw new IllegalArgumentException("Bloqueo no encontrado");
        }
        model.addAttribute("bloqueo", bloqueo);
        model.addAttribute("habitaciones", habitacionRepository.findAll());
        model.addAttribute("salas", salaRepository.findAll());
        return "admin/bloqueo-form";
    }

    @GetMapping("/bloqueos/eliminar/{id}")
    public String eliminarBloqueo(@PathVariable Long id) {
        bloqueoService.eliminarBloqueo(id);
        return "redirect:/admin/bloqueos";
    }
}
