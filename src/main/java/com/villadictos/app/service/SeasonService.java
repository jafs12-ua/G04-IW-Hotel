package com.villadictos.app.service;

import com.villadictos.app.model.Temporada;
import com.villadictos.app.repository.TemporadaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class SeasonService {

    private final TemporadaRepository temporadaRepository;

    public SeasonService(TemporadaRepository temporadaRepository) {
        this.temporadaRepository = temporadaRepository;
    }

    /**
     * Get the active season for a given date
     */
    public Optional<Temporada> getActiveSeason(LocalDate date) {
        return temporadaRepository.findActiveSeasonByDate(date);
    }

    /**
     * Get the season type (ALTA, MEDIA, BAJA) for a date
     */
    public String getSeasonType(LocalDate date) {
        return getActiveSeason(date)
                .map(Temporada::getSeasonType)
                .orElse("BAJA");
    }

    /**
     * Calculate price with season factor applied
     */
    public BigDecimal calculateSeasonPrice(BigDecimal basePrice, LocalDate date) {
        BigDecimal factor = getActiveSeason(date)
                .map(Temporada::getFactorPrecio)
                .orElse(BigDecimal.ONE);
        return basePrice.multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculate average price for a date range considering season changes
     */
    public BigDecimal calculateAveragePrice(BigDecimal basePrice, LocalDate checkIn, LocalDate checkOut) {
        var seasons = temporadaRepository.findSeasonsInRange(checkIn, checkOut);

        if (seasons.isEmpty()) {
            return basePrice;
        }

        // For simplicity, use the season at check-in date
        return calculateSeasonPrice(basePrice, checkIn);
    }
}
