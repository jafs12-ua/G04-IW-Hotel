package com.villadictos.app.dto.api;

import java.math.BigDecimal;

/**
 * DTO for service detail endpoint response
 */
public record ServiceDetailDTO(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String type) {
    public static ServiceDetailDTO fromServicio(com.villadictos.app.model.Servicio s) {
        return new ServiceDetailDTO(
                s.getId(),
                s.getNombre(),
                s.getDescripcion(),
                s.getPrecio(),
                s.getTipo().getValor());
    }
}
