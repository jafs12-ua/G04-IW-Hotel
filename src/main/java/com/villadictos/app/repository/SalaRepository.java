package com.villadictos.app.repository;

import com.villadictos.app.model.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalaRepository extends JpaRepository<Sala, Long> {

    /**
     * Check if a specific facility is available for given dates
     */
    @Query("""
                SELECT COUNT(s) > 0 FROM Sala s
                WHERE s.id = :salaId
                AND s.id NOT IN (
                    SELECT r.sala.id FROM Reserva r
                    WHERE r.sala IS NOT NULL
                    AND r.estado IN ('pendiente', 'confirmada')
                    AND r.fechaInicio < :endDate
                    AND r.fechaFin > :startDate
                )
                AND s.id NOT IN (
                    SELECT b.sala.id FROM Bloqueo b
                    WHERE b.sala IS NOT NULL
                    AND b.fechaInicio < :endDate
                    AND b.fechaFin > :startDate
                )
            """)
    boolean isFacilityAvailable(
            @Param("salaId") Long salaId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<Sala> findByAforoMaxGreaterThanEqual(Integer minCapacity);
}
