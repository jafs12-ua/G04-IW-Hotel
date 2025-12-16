package com.villadictos.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "tipos_habitacion")
public class TipoHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "capacidad_personas", nullable = false)
    private Integer capacidadPersonas;

    @Column(name = "precio_base_noche", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBaseNoche;

    @OneToMany(mappedBy = "tipoHabitacion")
    private List<Habitacion> habitaciones;

    public TipoHabitacion() {
    }

    public TipoHabitacion(String nombre, String descripcion, Integer capacidadPersonas, BigDecimal precioBaseNoche) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.capacidadPersonas = capacidadPersonas;
        this.precioBaseNoche = precioBaseNoche;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getCapacidadPersonas() {
        return capacidadPersonas;
    }

    public void setCapacidadPersonas(Integer capacidadPersonas) {
        this.capacidadPersonas = capacidadPersonas;
    }

    public BigDecimal getPrecioBaseNoche() {
        return precioBaseNoche;
    }

    public void setPrecioBaseNoche(BigDecimal precioBaseNoche) {
        this.precioBaseNoche = precioBaseNoche;
    }

    public List<Habitacion> getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(List<Habitacion> habitaciones) {
        this.habitaciones = habitaciones;
    }
}
