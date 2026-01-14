package com.villadictos.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ReservarMultiplesServiciosDTO {

    @NotNull(message = "Debes seleccionar una reserva")
    private Long idReserva;

    @NotEmpty(message = "Debes seleccionar al menos un servicio")
    @Valid
    private List<ServicioItemDTO> servicios;

    public ReservarMultiplesServiciosDTO() {
    }

    // Getters and Setters
    public Long getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Long idReserva) {
        this.idReserva = idReserva;
    }

    public List<ServicioItemDTO> getServicios() {
        return servicios;
    }

    public void setServicios(List<ServicioItemDTO> servicios) {
        this.servicios = servicios;
    }
}
