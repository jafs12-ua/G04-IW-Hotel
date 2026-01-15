package com.villadictos.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa un cliente externo de la API (empresa/proyecto)
 * que puede consumir los endpoints públicos con su API Key.
 */
@Entity
@Table(name = "api_clients")
public class ApiClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String empresa;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "api_key", nullable = false, unique = true, length = 64)
    private String apiKey;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "ultimo_uso")
    private LocalDateTime ultimoUso;

    @Column(name = "total_peticiones")
    private Long totalPeticiones = 0L;

    public ApiClient() {
        this.fechaCreacion = LocalDateTime.now();
        this.apiKey = generateApiKey();
    }

    public ApiClient(String nombre, String empresa, String email, String descripcion) {
        this.nombre = nombre;
        this.empresa = empresa;
        this.email = email;
        this.descripcion = descripcion;
        this.fechaCreacion = LocalDateTime.now();
        this.apiKey = generateApiKey();
    }

    private String generateApiKey() {
        return "vd_" + UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public void regenerateApiKey() {
        this.apiKey = generateApiKey();
    }

    public void registrarUso() {
        this.ultimoUso = LocalDateTime.now();
        this.totalPeticiones++;
    }

    // Getters y Setters
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

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getUltimoUso() {
        return ultimoUso;
    }

    public void setUltimoUso(LocalDateTime ultimoUso) {
        this.ultimoUso = ultimoUso;
    }

    public Long getTotalPeticiones() {
        return totalPeticiones;
    }

    public void setTotalPeticiones(Long totalPeticiones) {
        this.totalPeticiones = totalPeticiones;
    }
}
