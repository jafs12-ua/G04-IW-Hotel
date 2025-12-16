package com.villadictos.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "salas")
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "aforo_max", nullable = false)
    private Integer aforoMax;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String equipamiento;

    @Column(name = "precio_base_dia", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBaseDia;

    public Sala() {
    }

    public Sala(String nombre, Integer aforoMax, String descripcion, String equipamiento, BigDecimal precioBaseDia) {
        this.nombre = nombre;
        this.aforoMax = aforoMax;
        this.descripcion = descripcion;
        this.equipamiento = equipamiento;
        this.precioBaseDia = precioBaseDia;
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

    public Integer getAforoMax() {
        return aforoMax;
    }

    public void setAforoMax(Integer aforoMax) {
        this.aforoMax = aforoMax;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEquipamiento() {
        return equipamiento;
    }

    public void setEquipamiento(String equipamiento) {
        this.equipamiento = equipamiento;
    }

    public BigDecimal getPrecioBaseDia() {
        return precioBaseDia;
    }

    public void setPrecioBaseDia(BigDecimal precioBaseDia) {
        this.precioBaseDia = precioBaseDia;
    }
}
