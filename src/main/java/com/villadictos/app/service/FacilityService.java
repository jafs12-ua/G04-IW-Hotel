package com.villadictos.app.service;

import com.villadictos.app.dto.api.FacilityDetailDTO;
import com.villadictos.app.exception.FacilityNotFoundException;
import com.villadictos.app.model.Sala;
import com.villadictos.app.repository.SalaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FacilityService {

    private final SalaRepository salaRepository;

    public FacilityService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    /**
     * Get detailed facility information
     */
    public FacilityDetailDTO findFacilityById(Long id) {
        Sala facility = salaRepository.findById(id)
                .orElseThrow(() -> new FacilityNotFoundException(id));

        // Check availability for next 30 days
        LocalDate today = LocalDate.now();
        boolean available = salaRepository.isFacilityAvailable(id, today, today.plusDays(30));

        List<String> images = generateImageUrls(facility);

        return FacilityDetailDTO.fromSala(facility, available, images);
    }

    /**
     * Check if a facility is available for given dates
     */
    public boolean isFacilityAvailable(Long facilityId, LocalDate startDate, LocalDate endDate) {
        return salaRepository.isFacilityAvailable(facilityId, startDate, endDate);
    }

    /**
     * Get all facilities with optional capacity filter
     */
    public List<FacilityDetailDTO> findAllFacilities(Integer minCapacity) {
        List<Sala> facilities = minCapacity != null
                ? salaRepository.findByAforoMaxGreaterThanEqual(minCapacity)
                : salaRepository.findAll();

        LocalDate today = LocalDate.now();

        return facilities.stream()
                .map(f -> {
                    boolean available = salaRepository.isFacilityAvailable(f.getId(), today, today.plusDays(30));
                    return FacilityDetailDTO.fromSala(f, available, generateImageUrls(f));
                })
                .toList();
    }

    private List<String> generateImageUrls(Sala facility) {
        String baseUrl = "https://hotel.com/images/sala-";
        String safeName = facility.getNombre().toLowerCase().replace(" ", "-");
        return Arrays.asList(
                baseUrl + safeName + "-1.jpg",
                baseUrl + safeName + "-2.jpg");
    }
}
