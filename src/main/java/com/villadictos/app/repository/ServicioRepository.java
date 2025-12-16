package com.villadictos.app.repository;

import com.villadictos.app.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    Optional<Servicio> findByNombre(String nombre);

    List<Servicio> findByTipo(Servicio.TipoServicio tipo);
}
