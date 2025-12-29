package com.villadictos.app.service;

import com.villadictos.app.dto.CrearReservaDTO;
import com.villadictos.app.model.*;
import com.villadictos.app.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final HabitacionRepository habitacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final TemporadaRepository temporadaRepository;
    private final ModeloReservaRepository modeloReservaRepository;
    private final BloqueoRepository bloqueoRepository;

    public ReservaService(ReservaRepository reservaRepository,
            HabitacionRepository habitacionRepository,
            UsuarioRepository usuarioRepository,
            TemporadaRepository temporadaRepository,
            ModeloReservaRepository modeloReservaRepository,
            BloqueoRepository bloqueoRepository) {
        this.reservaRepository = reservaRepository;
        this.habitacionRepository = habitacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.temporadaRepository = temporadaRepository;
        this.modeloReservaRepository = modeloReservaRepository;
        this.bloqueoRepository = bloqueoRepository;
    }

    public List<Reserva> findAll() {
        return reservaRepository.findAll();
    }

    public List<Reserva> findReservasActivas() {
        return reservaRepository.findByEstado(Reserva.EstadoReserva.confirmada);
    }

    public List<Reserva> findReservasPendientes() {
        return reservaRepository.findByEstado(Reserva.EstadoReserva.pendiente);
    }

    public Reserva findById(Long id) {
        return reservaRepository.findById(id).orElse(null);
    }

    @Transactional
    public Reserva crearReserva(CrearReservaDTO dto) {
        // Validar fechas
        if (dto.getFechaFin().isBefore(dto.getFechaInicio()) || dto.getFechaFin().isEqual(dto.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio");
        }

        // Obtener habitación
        Habitacion habitacion = habitacionRepository.findById(dto.getIdHabitacion())
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));

        // Validar capacidad
        if (dto.getNumPersonas() > habitacion.getTipoHabitacion().getCapacidadPersonas()) {
            throw new IllegalArgumentException("La habitación no tiene capacidad suficiente");
        }

        // Verificar disponibilidad
        if (!habitacionRepository.isRoomAvailable(dto.getIdHabitacion(), dto.getFechaInicio(), dto.getFechaFin())) {
            throw new IllegalArgumentException("La habitación no está disponible en esas fechas");
        }

        // Verificar bloqueos
        if (bloqueoRepository.isRoomBlocked(dto.getIdHabitacion(), dto.getFechaInicio(), dto.getFechaFin())) {
            throw new IllegalArgumentException("La habitación está bloqueada en esas fechas");
        }

        // Obtener cliente
        Usuario cliente = usuarioRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        // Obtener temporada
        Temporada temporada = temporadaRepository.findActiveSeasonByDate(dto.getFechaInicio())
                .orElse(null);

        // Obtener modelo de reserva si existe
        ModeloReserva modeloReserva = null;
        if (dto.getIdModeloReserva() != null) {
            modeloReserva = modeloReservaRepository.findById(dto.getIdModeloReserva()).orElse(null);
        }

        // Calcular precio
        long noches = ChronoUnit.DAYS.between(dto.getFechaInicio(), dto.getFechaFin());
        BigDecimal precioBase = habitacion.getTipoHabitacion().getPrecioBaseNoche();
        BigDecimal factorTemporada = temporada != null ? temporada.getFactorPrecio() : BigDecimal.ONE;
        BigDecimal precioAdicional = modeloReserva != null ? modeloReserva.getPrecioAdicionalNoche() : BigDecimal.ZERO;

        BigDecimal precioNoche = precioBase.multiply(factorTemporada).add(precioAdicional);
        BigDecimal precioTotal = precioNoche.multiply(BigDecimal.valueOf(noches));

        // Crear reserva
        Reserva reserva = new Reserva();
        reserva.setUsuario(cliente);
        reserva.setHabitacion(habitacion);
        reserva.setFechaInicio(dto.getFechaInicio());
        reserva.setFechaFin(dto.getFechaFin());
        reserva.setNumPersonas(dto.getNumPersonas());
        reserva.setTemporada(temporada);
        reserva.setModeloReserva(modeloReserva);
        reserva.setPrecioTotal(precioTotal);
        reserva.setEstado(Reserva.EstadoReserva.confirmada);
        reserva.setNotas(dto.getNotas());

        return reservaRepository.save(reserva);
    }

    public long contarCheckInsHoy() {
        LocalDate hoy = LocalDate.now();
        return reservaRepository.findAll().stream()
                .filter(r -> r.getFechaInicio().equals(hoy))
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada)
                .count();
    }

    public long contarCheckOutsHoy() {
        LocalDate hoy = LocalDate.now();
        return reservaRepository.findAll().stream()
                .filter(r -> r.getFechaFin().equals(hoy))
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada)
                .count();
    }

    public long contarReservasPendientes() {
        return reservaRepository.findByEstado(Reserva.EstadoReserva.pendiente).size();
    }

    public long contarHabitacionesOcupadasHoy() {
        LocalDate hoy = LocalDate.now();
        return reservaRepository.findAll().stream()
                .filter(r -> r.getHabitacion() != null)
                .filter(r -> !r.getFechaInicio().isAfter(hoy) && !r.getFechaFin().isBefore(hoy))
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada)
                .map(r -> r.getHabitacion().getId())
                .distinct()
                .count();
    }

    public List<Reserva> findCheckInsHoy() {
        LocalDate hoy = LocalDate.now();
        return reservaRepository.findAll().stream()
                .filter(r -> r.getFechaInicio().equals(hoy))
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada)
                .toList();
    }

    public List<Reserva> findCheckOutsHoy() {
        LocalDate hoy = LocalDate.now();
        return reservaRepository.findAll().stream()
                .filter(r -> r.getFechaFin().equals(hoy))
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada)
                .toList();
    }
}
