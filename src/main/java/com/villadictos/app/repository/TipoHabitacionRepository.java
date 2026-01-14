package com.villadictos.app.repository;

import com.villadictos.app.model.TipoHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TipoHabitacionRepository extends JpaRepository<TipoHabitacion, Long> {
    Optional<TipoHabitacion> findByNombre(String nombre);
    
    /**
     * Find room types that have at least one available room during the given date range
     * and can accommodate the specified number of guests
     */
    @Query("""
        SELECT DISTINCT th FROM TipoHabitacion th
        WHERE th.capacidadPersonas >= :cantidadAdultos
        AND EXISTS (
            SELECT h FROM Habitacion h
            WHERE h.tipoHabitacion.id = th.id
            AND h.id NOT IN (
                SELECT r.habitacion.id FROM Reserva r
                WHERE r.habitacion IS NOT NULL
                AND r.estado IN ('pendiente', 'confirmada')
                AND r.fechaInicio < :checkOut
                AND r.fechaFin > :checkIn
            )
            AND h.id NOT IN (
                SELECT b.habitacion.id FROM Bloqueo b
                WHERE b.habitacion IS NOT NULL
                AND b.fechaInicio < :checkOut
                AND b.fechaFin > :checkIn
            )
        )
        ORDER BY th.precioBaseNoche ASC
    """)
    List<TipoHabitacion> findAvailableByCapacityAndDateRange(
            @Param("cantidadAdultos") Integer cantidadAdultos,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);
}
