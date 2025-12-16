package com.villadictos.app.repository;

import com.villadictos.app.model.Temporada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TemporadaRepository extends JpaRepository<Temporada, Long> {

    /**
     * Find the active season for a given date
     */
    @Query("""
                SELECT t FROM Temporada t
                WHERE :date BETWEEN t.fechaInicio AND t.fechaFin
                ORDER BY t.factorPrecio DESC
            """)
    Optional<Temporada> findActiveSeasonByDate(@Param("date") LocalDate date);

    /**
     * Find seasons that overlap with a date range
     */
    @Query("""
                SELECT t FROM Temporada t
                WHERE t.fechaInicio <= :endDate AND t.fechaFin >= :startDate
                ORDER BY t.fechaInicio
            """)
    java.util.List<Temporada> findSeasonsInRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
