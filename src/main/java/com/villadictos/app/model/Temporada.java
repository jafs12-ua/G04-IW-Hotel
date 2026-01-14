package com.villadictos.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "temporadas")
public class Temporada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "factor_precio", nullable = false, precision = 5, scale = 2)
    private BigDecimal factorPrecio = BigDecimal.ONE;

    public Temporada() {
    }

    public Temporada(String nombre, LocalDate fechaInicio, LocalDate fechaFin, BigDecimal factorPrecio) {
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.factorPrecio = factorPrecio;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getFactorPrecio() {
        return factorPrecio;
    }

    public void setFactorPrecio(BigDecimal factorPrecio) {
        this.factorPrecio = factorPrecio;
    }

    /**
     * Map season to API format (ALTA, MEDIA, BAJA)
     */
    public String getSeasonType() {
        if (factorPrecio.compareTo(new BigDecimal("1.40")) >= 0) {
            return "ALTA";
        } else if (factorPrecio.compareTo(new BigDecimal("1.10")) >= 0) {
            return "MEDIA";
        } else {
            return "BAJA";
        }
    }
}
