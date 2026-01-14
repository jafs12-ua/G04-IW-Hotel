package com.villadictos.app.service;

import com.villadictos.app.dto.BloqueoDTO;
import com.villadictos.app.model.Bloqueo;
import com.villadictos.app.model.Habitacion;
import com.villadictos.app.model.Usuario;
import com.villadictos.app.repository.BloqueoRepository;
import com.villadictos.app.repository.HabitacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BloqueoService {

    private final BloqueoRepository bloqueoRepository;
    private final HabitacionRepository habitacionRepository;

    public BloqueoService(BloqueoRepository bloqueoRepository, HabitacionRepository habitacionRepository) {
        this.bloqueoRepository = bloqueoRepository;
        this.habitacionRepository = habitacionRepository;
    }

    public List<Bloqueo> findAll() {
        return bloqueoRepository.findAll();
    }

    public List<Bloqueo> findBloqueosActivos() {
        LocalDate hoy = LocalDate.now();
        return bloqueoRepository.findAll().stream()
                .filter(b -> !b.getFechaFin().isBefore(hoy))
                .toList();
    }

    public Bloqueo findById(Long id) {
        return bloqueoRepository.findById(id).orElse(null);
    }

    @Transactional
    public Bloqueo crearBloqueo(BloqueoDTO dto, Usuario creador) {
        Habitacion habitacion = habitacionRepository.findById(dto.getIdHabitacion())
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));

        // Verificar que no haya conflictos
        if (bloqueoRepository.isRoomBlocked(dto.getIdHabitacion(), dto.getFechaInicio(), dto.getFechaFin())) {
            throw new IllegalArgumentException("La habitación ya tiene un bloqueo en esas fechas");
        }

        Bloqueo bloqueo = new Bloqueo();
        bloqueo.setHabitacion(habitacion);
        bloqueo.setFechaInicio(dto.getFechaInicio());
        bloqueo.setFechaFin(dto.getFechaFin());
        bloqueo.setMotivo(dto.getMotivo());
        bloqueo.setUsuarioCreador(creador);

        return bloqueoRepository.save(bloqueo);
    }

    @Transactional
    public Bloqueo save(Bloqueo bloqueo) {
        return bloqueoRepository.save(bloqueo);
    }

    @Transactional
    public void eliminarBloqueo(Long id) {
        bloqueoRepository.deleteById(id);
    }

    public long contarHabitacionesBloqueadasHoy() {
        LocalDate hoy = LocalDate.now();
        return bloqueoRepository.findAll().stream()
                .filter(b -> b.getHabitacion() != null)
                .filter(b -> !b.getFechaInicio().isAfter(hoy) && !b.getFechaFin().isBefore(hoy))
                .map(b -> b.getHabitacion().getId())
                .distinct()
                .count();
    }
}
