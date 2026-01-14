package com.villadictos.app.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO para crear un nuevo bloqueo de habitación
 */
public class BloqueoDTO {

    private Long id;

    @NotNull(message = "La habitación es obligatoria")
    private Long idHabitacion;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio debe ser hoy o futura")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 500, message = "El motivo no puede superar 500 caracteres")
    private String motivo;

    public BloqueoDTO() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdHabitacion() {
        return idHabitacion;
    }

    public void setIdHabitacion(Long idHabitacion) {
        this.idHabitacion = idHabitacion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
