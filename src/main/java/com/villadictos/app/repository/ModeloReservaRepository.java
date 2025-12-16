package com.villadictos.app.repository;

import com.villadictos.app.model.ModeloReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModeloReservaRepository extends JpaRepository<ModeloReserva, Long> {
    Optional<ModeloReserva> findByNombre(String nombre);
}
