package com.villadictos.app.dto.api;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for facility detail endpoint response
 */
public record FacilityDetailDTO(
        Long id,
        String name,
        String type,
        Integer capacity,
        BigDecimal pricePerHour,
        String description,
        List<String> amenities,
        List<String> images,
        ScheduleDTO schedule,
        Boolean available) {
    public record ScheduleDTO(
            String openTime,
            String closeTime) {
    }

    public static FacilityDetailDTO fromSala(
            com.villadictos.app.model.Sala s,
            Boolean available,
            List<String> images) {
        // Parse equipamiento as amenities
        List<String> amenities = s.getEquipamiento() != null
                ? List.of(s.getEquipamiento().split(",\\s*"))
                : List.of();

        return new FacilityDetailDTO(
                s.getId(),
                s.getNombre(),
                "CONFERENCE_ROOM",
                s.getAforoMax(),
                s.getPrecioBaseDia().divide(BigDecimal.valueOf(8), 2, java.math.RoundingMode.HALF_UP), // Convert day
                                                                                                       // rate to hourly
                s.getDescripcion(),
                amenities,
                images,
                new ScheduleDTO("08:00", "22:00"),
                available);
    }
}
