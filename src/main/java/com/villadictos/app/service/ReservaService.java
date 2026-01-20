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
    private final TipoHabitacionRepository tipoHabitacionRepository;
    private final SalaRepository salaRepository;

    public ReservaService(ReservaRepository reservaRepository,
            HabitacionRepository habitacionRepository,
            UsuarioRepository usuarioRepository,
            TemporadaRepository temporadaRepository,
            ModeloReservaRepository modeloReservaRepository,
            BloqueoRepository bloqueoRepository,
            TipoHabitacionRepository tipoHabitacionRepository,
            SalaRepository salaRepository) {
        this.reservaRepository = reservaRepository;
        this.habitacionRepository = habitacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.temporadaRepository = temporadaRepository;
        this.modeloReservaRepository = modeloReservaRepository;
        this.bloqueoRepository = bloqueoRepository;
        this.tipoHabitacionRepository = tipoHabitacionRepository;
        this.salaRepository = salaRepository;
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

    public Reserva save(Reserva reserva) {
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva crearReserva(CrearReservaDTO dto) {
        // Validar fechas
        if (dto.getFechaFin().isBefore(dto.getFechaInicio()) || dto.getFechaFin().isEqual(dto.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio");
        }

        // Obtener cliente
        Usuario cliente = usuarioRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        // Determinar tipo de reserva
        if (dto.getTipoReserva() == CrearReservaDTO.TipoReserva.SALA) {
            return crearReservaSala(dto, cliente);
        } else {
            return crearReservaHabitacion(dto, cliente);
        }
    }

    private Reserva crearReservaHabitacion(CrearReservaDTO dto, Usuario cliente) {
        Habitacion habitacion;
        TipoHabitacion tipoHabitacion;

        // Si se proporciona una habitación específica (desde recepción), usarla
        // directamente
        if (dto.getIdHabitacion() != null) {
            habitacion = habitacionRepository.findById(dto.getIdHabitacion())
                    .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));
            tipoHabitacion = habitacion.getTipoHabitacion();

            // Verificar disponibilidad de la habitación específica
            boolean disponible = habitacionRepository.isRoomAvailable(
                    dto.getIdHabitacion(), dto.getFechaInicio(), dto.getFechaFin());
            if (!disponible) {
                throw new IllegalArgumentException("La habitación " + habitacion.getNumeroHabitacion()
                        + " no está disponible para las fechas seleccionadas");
            }
        }
        // Si se proporciona un tipo de habitación (desde cliente), buscar una
        // disponible
        else if (dto.getIdTipoHabitacion() != null) {
            tipoHabitacion = tipoHabitacionRepository.findById(dto.getIdTipoHabitacion())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de habitación no encontrado"));

            // Buscar habitaciones disponibles del tipo seleccionado
            List<Habitacion> habitacionesDisponibles = habitacionRepository.findAvailableByTypeAndDateRange(
                    dto.getIdTipoHabitacion(),
                    dto.getFechaInicio(),
                    dto.getFechaFin());

            if (habitacionesDisponibles.isEmpty()) {
                throw new IllegalArgumentException("No hay habitaciones disponibles del tipo "
                        + tipoHabitacion.getNombre() + " para las fechas seleccionadas");
            }

            // Asignar la primera habitación disponible
            habitacion = habitacionesDisponibles.get(0);
        } else {
            throw new IllegalArgumentException("Debe seleccionar una habitación o tipo de habitación");
        }

        // Validar capacidad
        if (dto.getNumPersonas() > tipoHabitacion.getCapacidadPersonas()) {
            throw new IllegalArgumentException("La habitación seleccionada no tiene capacidad suficiente para "
                    + dto.getNumPersonas() + " personas. Capacidad máxima: " + tipoHabitacion.getCapacidadPersonas());
        }

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

    private Reserva crearReservaSala(CrearReservaDTO dto, Usuario cliente) {
        // Validar sala seleccionada
        if (dto.getIdSala() == null) {
            throw new IllegalArgumentException("Debe seleccionar una sala");
        }

        Sala sala = salaRepository.findById(dto.getIdSala())
                .orElseThrow(() -> new IllegalArgumentException("Sala no encontrada"));

        // Validar capacidad
        if (dto.getNumPersonas() > sala.getAforoMax()) {
            throw new IllegalArgumentException("La sala seleccionada no tiene capacidad suficiente para "
                    + dto.getNumPersonas() + " personas. Aforo máximo: " + sala.getAforoMax());
        }

        // Verificar disponibilidad de la sala
        List<Reserva> reservasExistentes = reservaRepository.findBySalaIdAndFechaRange(
                dto.getIdSala(),
                dto.getFechaInicio(),
                dto.getFechaFin());

        if (!reservasExistentes.isEmpty()) {
            throw new IllegalArgumentException("La sala '" + sala.getNombre()
                    + "' no está disponible para las fechas seleccionadas");
        }

        // Calcular precio (precio por día)
        long dias = ChronoUnit.DAYS.between(dto.getFechaInicio(), dto.getFechaFin());
        BigDecimal precioTotal = sala.getPrecioBaseDia().multiply(BigDecimal.valueOf(dias));

        // Crear reserva
        Reserva reserva = new Reserva();
        reserva.setUsuario(cliente);
        reserva.setSala(sala);
        reserva.setFechaInicio(dto.getFechaInicio());
        reserva.setFechaFin(dto.getFechaFin());
        reserva.setNumPersonas(dto.getNumPersonas());
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
