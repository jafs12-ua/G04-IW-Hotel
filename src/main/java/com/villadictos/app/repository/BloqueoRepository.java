package com.villadictos.app.repository;

import com.villadictos.app.model.Bloqueo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface BloqueoRepository extends JpaRepository<Bloqueo, Long> {

    /**
     * Check if room is blocked for given dates
     */
    @Query("""
                SELECT COUNT(b) > 0 FROM Bloqueo b
                WHERE b.habitacion.id = :roomId
                AND b.fechaInicio < :checkOut
                AND b.fechaFin > :checkIn
            """)
    boolean isRoomBlocked(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    /**
     * Check if facility is blocked for given dates
     */
    @Query("""
                SELECT COUNT(b) > 0 FROM Bloqueo b
                WHERE b.sala.id = :salaId
                AND b.fechaInicio < :endDate
                AND b.fechaFin > :startDate
            """)
    boolean isFacilityBlocked(
            @Param("salaId") Long salaId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
