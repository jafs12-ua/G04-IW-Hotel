package com.villadictos.app.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO para crear una nueva reserva (habitación o sala)
 */
public class CrearReservaDTO {

    public enum TipoReserva {
        HABITACION, SALA
    }

    @NotNull(message = "El cliente es obligatorio")
    private Long idCliente;

    private TipoReserva tipoReserva = TipoReserva.HABITACION;

    // Para reservas de habitación
    private Long idTipoHabitacion;

    // Para reservas de habitación específica (desde recepción)
    private Long idHabitacion;

    // Para reservas de sala
    private Long idSala;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio debe ser hoy o futura")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Future(message = "La fecha de fin debe ser futura")
    private LocalDate fechaFin;

    @Min(value = 1, message = "Mínimo 1 persona")
    @Max(value = 200, message = "Máximo 200 personas")
    private int numPersonas = 1;

    private Long idModeloReserva;

    private String notas;

    public CrearReservaDTO() {
    }

    // Getters y Setters
    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public TipoReserva getTipoReserva() {
        return tipoReserva;
    }

    public void setTipoReserva(TipoReserva tipoReserva) {
        this.tipoReserva = tipoReserva;
    }

    public Long getIdTipoHabitacion() {
        return idTipoHabitacion;
    }

    public void setIdTipoHabitacion(Long idTipoHabitacion) {
        this.idTipoHabitacion = idTipoHabitacion;
    }

    public Long getIdHabitacion() {
        return idHabitacion;
    }

    public void setIdHabitacion(Long idHabitacion) {
        this.idHabitacion = idHabitacion;
    }

    public Long getIdSala() {
        return idSala;
    }

    public void setIdSala(Long idSala) {
        this.idSala = idSala;
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

    public int getNumPersonas() {
        return numPersonas;
    }

    public void setNumPersonas(int numPersonas) {
        this.numPersonas = numPersonas;
    }

    public Long getIdModeloReserva() {
        return idModeloReserva;
    }

    public void setIdModeloReserva(Long idModeloReserva) {
        this.idModeloReserva = idModeloReserva;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
