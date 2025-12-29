package com.villadictos.app.service;

import com.villadictos.app.dto.DashboardStatsDTO;
import com.villadictos.app.dto.HabitacionEstadoDTO;
import com.villadictos.app.model.*;
import com.villadictos.app.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecepcionService {

    private final HabitacionRepository habitacionRepository;
    private final ReservaRepository reservaRepository;
    private final BloqueoRepository bloqueoRepository;
    private final TemporadaRepository temporadaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaService reservaService;
    private final BloqueoService bloqueoService;

    public RecepcionService(HabitacionRepository habitacionRepository,
            ReservaRepository reservaRepository,
            BloqueoRepository bloqueoRepository,
            TemporadaRepository temporadaRepository,
            UsuarioRepository usuarioRepository,
            ReservaService reservaService,
            BloqueoService bloqueoService) {
        this.habitacionRepository = habitacionRepository;
        this.reservaRepository = reservaRepository;
        this.bloqueoRepository = bloqueoRepository;
        this.temporadaRepository = temporadaRepository;
        this.usuarioRepository = usuarioRepository;
        this.reservaService = reservaService;
        this.bloqueoService = bloqueoService;
    }

    public DashboardStatsDTO obtenerEstadisticas() {
        LocalDate hoy = LocalDate.now();
        DashboardStatsDTO stats = new DashboardStatsDTO();

        long totalHabitaciones = habitacionRepository.count();
        long ocupadas = reservaService.contarHabitacionesOcupadasHoy();
        long bloqueadas = bloqueoService.contarHabitacionesBloqueadasHoy();
        long libres = totalHabitaciones - ocupadas - bloqueadas;
        if (libres < 0)
            libres = 0;

        stats.setTotalHabitaciones(totalHabitaciones);
        stats.setHabitacionesOcupadas(ocupadas);
        stats.setHabitacionesBloqueadas(bloqueadas);
        stats.setHabitacionesLibres(libres);

        // Temporada actual
        temporadaRepository.findActiveSeasonByDate(hoy).ifPresentOrElse(
                t -> {
                    stats.setTemporadaActual(t.getNombre());
                    stats.setFactorPrecio(t.getFactorPrecio());
                },
                () -> {
                    stats.setTemporadaActual("Sin temporada definida");
                    stats.setFactorPrecio(BigDecimal.ONE);
                });

        stats.setCheckInsHoy(reservaService.contarCheckInsHoy());
        stats.setCheckOutsHoy(reservaService.contarCheckOutsHoy());
        stats.setReservasPendientes(reservaService.contarReservasPendientes());
        stats.setTotalClientes(contarClientes());

        return stats;
    }

    public List<HabitacionEstadoDTO> obtenerEstadoHabitaciones() {
        LocalDate hoy = LocalDate.now();
        List<Habitacion> habitaciones = habitacionRepository.findAll();
        List<HabitacionEstadoDTO> resultado = new ArrayList<>();

        for (Habitacion h : habitaciones) {
            HabitacionEstadoDTO.Estado estado = HabitacionEstadoDTO.Estado.LIBRE;
            String detalle = null;

            // Verificar si está bloqueada
            boolean bloqueada = bloqueoRepository.isRoomBlocked(h.getId(), hoy, hoy.plusDays(1));
            if (bloqueada) {
                estado = HabitacionEstadoDTO.Estado.BLOQUEADA;
                // Buscar motivo del bloqueo
                List<Bloqueo> bloqueos = bloqueoRepository.findAll().stream()
                        .filter(b -> b.getHabitacion() != null && b.getHabitacion().getId().equals(h.getId()))
                        .filter(b -> !b.getFechaInicio().isAfter(hoy) && !b.getFechaFin().isBefore(hoy))
                        .toList();
                if (!bloqueos.isEmpty()) {
                    detalle = bloqueos.get(0).getMotivo();
                }
            } else {
                // Verificar si está ocupada
                List<Reserva> reservas = reservaRepository.findAll().stream()
                        .filter(r -> r.getHabitacion() != null && r.getHabitacion().getId().equals(h.getId()))
                        .filter(r -> !r.getFechaInicio().isAfter(hoy) && !r.getFechaFin().isBefore(hoy))
                        .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada)
                        .toList();
                if (!reservas.isEmpty()) {
                    estado = HabitacionEstadoDTO.Estado.OCUPADA;
                    detalle = reservas.get(0).getUsuario().getNombre();
                }
            }

            resultado.add(HabitacionEstadoDTO.fromHabitacion(h, estado, detalle));
        }

        return resultado;
    }

    public List<Usuario> obtenerClientes() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() == Usuario.Rol.cliente)
                .toList();
    }

    public long contarClientes() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() == Usuario.Rol.cliente)
                .count();
    }
}
