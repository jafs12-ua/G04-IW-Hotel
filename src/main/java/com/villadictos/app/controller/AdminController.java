package com.villadictos.app.controller;

import com.villadictos.app.model.*;
import com.villadictos.app.repository.*;
import com.villadictos.app.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final HabitacionRepository habitacionRepository;
    private final TipoHabitacionRepository tipoHabitacionRepository;
    private final SalaRepository salaRepository;
    private final TemporadaRepository temporadaRepository;
    private final ServicioRepository servicioRepository;
    private final ModeloReservaRepository modeloReservaRepository;
    private final ReservaRepository reservaRepository;

    private final RecepcionService recepcionService;
    private final ReservaService reservaService;
    private final UsuarioService usuarioService;
    private final BloqueoService bloqueoService;
    private final PagoService pagoService;
    private final ReportService reportService;

    private final PasswordEncoder passwordEncoder;

    public AdminController(UsuarioRepository usuarioRepository, HabitacionRepository habitacionRepository,
            TipoHabitacionRepository tipoHabitacionRepository, SalaRepository salaRepository,
            TemporadaRepository temporadaRepository, ServicioRepository servicioRepository,
            ModeloReservaRepository modeloReservaRepository, ReservaRepository reservaRepository,
            RecepcionService recepcionService, ReservaService reservaService, UsuarioService usuarioService,
            BloqueoService bloqueoService, PagoService pagoService, ReportService reportService,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.habitacionRepository = habitacionRepository;
        this.tipoHabitacionRepository = tipoHabitacionRepository;
        this.salaRepository = salaRepository;
        this.temporadaRepository = temporadaRepository;
        this.servicioRepository = servicioRepository;
        this.modeloReservaRepository = modeloReservaRepository;
        this.reservaRepository = reservaRepository;
        this.recepcionService = recepcionService;
        this.reservaService = reservaService;
        this.usuarioService = usuarioService;
        this.bloqueoService = bloqueoService;
        this.pagoService = pagoService;
        this.reportService = reportService;
        this.passwordEncoder = passwordEncoder;
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

    // --- USUARIOS ---
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.findAll());
        return "admin/usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String formularioUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "admin/usuario-form";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, @RequestParam(required = false) String newPassword) {
        usuarioService.guardarUsuarioAdmin(usuario, newPassword);
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.findById(id).orElseThrow());
        return "admin/usuario-form";
    }

    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id) {
        usuarioService.deleteById(id);
        return "redirect:/admin/usuarios";
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

    // --- TEMPORADAS ---
    @GetMapping("/temporadas")
    public String listarTemporadas(Model model) {
        model.addAttribute("temporadas", temporadaRepository.findAll());
        return "admin/temporadas";
    }

    @GetMapping("/temporadas/nuevo")
    public String formularioTemporada(Model model) {
        model.addAttribute("temporada", new Temporada());
        return "admin/temporada-form";
    }

    @PostMapping("/temporadas/guardar")
    public String guardarTemporada(@ModelAttribute Temporada temporada) {
        temporadaRepository.save(temporada);
        return "redirect:/admin/temporadas";
    }

    @GetMapping("/temporadas/editar/{id}")
    public String editarTemporada(@PathVariable Long id, Model model) {
        model.addAttribute("temporada", temporadaRepository.findById(id).orElseThrow());
        return "admin/temporada-form";
    }

    @GetMapping("/temporadas/eliminar/{id}")
    public String eliminarTemporada(@PathVariable Long id) {
        temporadaRepository.deleteById(id);
        return "redirect:/admin/temporadas";
    }

    // --- SERVICIOS ---
    @GetMapping("/servicios")
    public String listarServicios(Model model) {
        model.addAttribute("servicios", servicioRepository.findAll());
        return "admin/servicios";
    }

    @GetMapping("/servicios/nuevo")
    public String formularioServicio(Model model) {
        model.addAttribute("servicio", new Servicio());
        return "admin/servicio-form";
    }

    @PostMapping("/servicios/guardar")
    public String guardarServicio(@ModelAttribute Servicio servicio) {
        servicioRepository.save(servicio);
        return "redirect:/admin/servicios";
    }

    @GetMapping("/servicios/editar/{id}")
    public String editarServicio(@PathVariable Long id, Model model) {
        model.addAttribute("servicio", servicioRepository.findById(id).orElseThrow());
        return "admin/servicio-form";
    }

    @GetMapping("/servicios/eliminar/{id}")
    public String eliminarServicio(@PathVariable Long id) {
        servicioRepository.deleteById(id);
        return "redirect:/admin/servicios";
    }

    // --- MODELOS RESERVA ---
    @GetMapping("/modelos")
    public String listarModelos(Model model) {
        model.addAttribute("modelos", modeloReservaRepository.findAll());
        return "admin/modelos";
    }

    @GetMapping("/modelos/nuevo")
    public String formularioModelo(Model model) {
        model.addAttribute("modelo", new ModeloReserva());
        return "admin/modelo-form";
    }

    @PostMapping("/modelos/guardar")
    public String guardarModelo(@ModelAttribute ModeloReserva modelo) {
        modeloReservaRepository.save(modelo);
        return "redirect:/admin/modelos";
    }

    @GetMapping("/modelos/editar/{id}")
    public String editarModelo(@PathVariable Long id, Model model) {
        model.addAttribute("modelo", modeloReservaRepository.findById(id).orElseThrow());
        return "admin/modelo-form";
    }

    @GetMapping("/modelos/eliminar/{id}")
    public String eliminarModelo(@PathVariable Long id) {
        modeloReservaRepository.deleteById(id);
        return "redirect:/admin/modelos";
    }

    // --- RESERVAS ---
    @GetMapping("/reservas")
    public String listarReservas(Model model) {
        model.addAttribute("reservas", reservaService.findAll());
        return "admin/reservas";
    }

    @GetMapping("/reservas/editar/{id}")
    public String editarReserva(@PathVariable Long id, Model model) {
        model.addAttribute("reserva", reservaService.findById(id));
        model.addAttribute("habitaciones", habitacionRepository.findAll());
        model.addAttribute("temporadas", temporadaRepository.findAll());
        model.addAttribute("modelos", modeloReservaRepository.findAll());
        model.addAttribute("estados", Reserva.EstadoReserva.values());
        return "admin/reserva-form";
    }

    @PostMapping("/reservas/guardar")
    public String guardarReserva(@ModelAttribute Reserva reserva) {
        // Guardar reserva directamente
        reservaRepository.save(reserva);
        return "redirect:/admin/reservas";
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

    // --- PAGOS ---
    @GetMapping("/pagos")
    public String listarPagos(Model model) {
        model.addAttribute("pagos", pagoService.findAll());
        return "admin/pagos";
    }

    // --- INFORMES ---
    @GetMapping("/informes")
    public String informes(@RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Model model) {

        LocalDate now = LocalDate.now();
        int currentMonth = month != null ? month : now.getMonthValue();
        int currentYear = year != null ? year : now.getYear();

        model.addAttribute("stats", reportService.getMonthlyStats(currentMonth, currentYear));
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("currentYear", currentYear);

        return "admin/informes";
    }

    @GetMapping("/informes/pdf")
    public org.springframework.http.ResponseEntity<byte[]> descargarInformeMensual(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        LocalDate now = LocalDate.now();
        int currentMonth = month != null ? month : now.getMonthValue();
        int currentYear = year != null ? year : now.getYear();

        try {
            byte[] pdfBytes = reportService.generateMonthlyReport(currentMonth, currentYear);

            return org.springframework.http.ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=informe_mensual_" + currentYear + "_" + currentMonth + ".pdf")
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/informes/pdf/anual")
    public org.springframework.http.ResponseEntity<byte[]> descargarInformeAnual(
            @RequestParam(required = false) Integer year) {

        LocalDate now = LocalDate.now();
        int currentYear = year != null ? year : now.getYear();

        try {
            byte[] pdfBytes = reportService.generateAnnualReport(currentYear);

            return org.springframework.http.ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=informe_anual_" + currentYear + ".pdf")
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return org.springframework.http.ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/informes/test-download")
    public void testDownload(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        String content = "Hola, esto es una prueba de descarga.";
        byte[] bytes = content.getBytes();

        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment; filename=\"prueba.txt\"");
        response.setContentLength(bytes.length);

        try (java.io.OutputStream out = response.getOutputStream()) {
            out.write(bytes);
            out.flush();
        }
    }
}