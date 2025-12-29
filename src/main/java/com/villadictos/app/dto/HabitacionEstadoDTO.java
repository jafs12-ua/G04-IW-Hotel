package com.villadictos.app.dto;

import com.villadictos.app.model.Habitacion;
import java.math.BigDecimal;

/**
 * DTO para mostrar el estado de una habitación
 */
public class HabitacionEstadoDTO {

    public enum Estado {
        LIBRE, OCUPADA, BLOQUEADA
    }

    private Long id;
    private String numeroHabitacion;
    private String tipoHabitacion;
    private int capacidad;
    private int planta;
    private String vistas;
    private BigDecimal precioBase;
    private Estado estado;
    private String detalleEstado; // Nombre del cliente o motivo del bloqueo

    public HabitacionEstadoDTO() {
    }

    public static HabitacionEstadoDTO fromHabitacion(Habitacion h, Estado estado, String detalleEstado) {
        HabitacionEstadoDTO dto = new HabitacionEstadoDTO();
        dto.setId(h.getId());
        dto.setNumeroHabitacion(h.getNumeroHabitacion());
        dto.setTipoHabitacion(h.getTipoHabitacion().getNombre());
        dto.setCapacidad(h.getTipoHabitacion().getCapacidadPersonas());
        dto.setPlanta(h.getPlanta());
        dto.setVistas(h.getVistas());
        dto.setPrecioBase(h.getTipoHabitacion().getPrecioBaseNoche());
        dto.setEstado(estado);
        dto.setDetalleEstado(detalleEstado);
        return dto;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroHabitacion() {
        return numeroHabitacion;
    }

    public void setNumeroHabitacion(String numeroHabitacion) {
        this.numeroHabitacion = numeroHabitacion;
    }

    public String getTipoHabitacion() {
        return tipoHabitacion;
    }

    public void setTipoHabitacion(String tipoHabitacion) {
        this.tipoHabitacion = tipoHabitacion;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public int getPlanta() {
        return planta;
    }

    public void setPlanta(int planta) {
        this.planta = planta;
    }

    public String getVistas() {
        return vistas;
    }

    public void setVistas(String vistas) {
        this.vistas = vistas;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public String getDetalleEstado() {
        return detalleEstado;
    }

    public void setDetalleEstado(String detalleEstado) {
        this.detalleEstado = detalleEstado;
    }
}
