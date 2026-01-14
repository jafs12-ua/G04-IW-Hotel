package com.villadictos.app.service;

import com.villadictos.app.dto.ReservarServicioDTO;
import com.villadictos.app.model.Reserva;
import com.villadictos.app.model.ReservaServicio;
import com.villadictos.app.model.Servicio;
import com.villadictos.app.repository.ReservaRepository;
import com.villadictos.app.repository.ReservaServicioRepository;
import com.villadictos.app.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReservaServicioService {

    private final ReservaServicioRepository reservaServicioRepository;
    private final ReservaRepository reservaRepository;
    private final ServicioRepository servicioRepository;

    public ReservaServicioService(ReservaServicioRepository reservaServicioRepository,
            ReservaRepository reservaRepository,
            ServicioRepository servicioRepository) {
        this.reservaServicioRepository = reservaServicioRepository;
        this.reservaRepository = reservaRepository;
        this.servicioRepository = servicioRepository;
    }

    @Transactional
    public ReservaServicio reservarServicio(ReservarServicioDTO dto) {
        // Obtener reserva
        Reserva reserva = reservaRepository.findById(dto.getIdReserva())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        // Verificar que la reserva esté confirmada
        if (reserva.getEstado() != Reserva.EstadoReserva.confirmada &&
                reserva.getEstado() != Reserva.EstadoReserva.pendiente) {
            throw new IllegalArgumentException("Solo puedes agregar servicios a reservas activas");
        }

        // Obtener servicio
        Servicio servicio = servicioRepository.findById(dto.getIdServicio())
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        // Validar fechas si se proporcionan
        if (dto.getFechaInicio() != null && dto.getFechaFin() != null) {
            if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
                throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
            }

            // Verificar que las fechas estén dentro del rango de la reserva
            if (dto.getFechaInicio().isBefore(reserva.getFechaInicio()) ||
                    dto.getFechaFin().isAfter(reserva.getFechaFin())) {
                throw new IllegalArgumentException(
                        "Las fechas del servicio deben estar dentro del período de tu reserva");
            }
        }

        // Crear reserva de servicio
        ReservaServicio reservaServicio = new ReservaServicio();
        reservaServicio.setReserva(reserva);
        reservaServicio.setServicio(servicio);
        reservaServicio.setCantidad(dto.getCantidad());
        reservaServicio.setPrecioUnitario(servicio.getPrecio());
        reservaServicio.setSubtotal(servicio.getPrecio().multiply(BigDecimal.valueOf(dto.getCantidad())));
        reservaServicio.setFechaInicio(dto.getFechaInicio());
        reservaServicio.setFechaFin(dto.getFechaFin());

        return reservaServicioRepository.save(reservaServicio);
    }

    public List<ReservaServicio> findByReservaId(Long reservaId) {
        return reservaServicioRepository.findByReservaId(reservaId);
    }

    public java.util.Optional<ReservaServicio> findById(Long id) {
        return reservaServicioRepository.findById(id);
    }

    public ReservaServicio save(ReservaServicio reservaServicio) {
        return reservaServicioRepository.save(reservaServicio);
    }

    @Transactional
    public void delete(ReservaServicio reservaServicio) {
        reservaServicioRepository.delete(reservaServicio);
    }
}
