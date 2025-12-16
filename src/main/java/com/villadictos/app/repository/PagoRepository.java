package com.villadictos.app.repository;

import com.villadictos.app.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByReservaId(Long reservaId);

    Optional<Pago> findByCodTransaccionTpv(String codTransaccionTpv);
}
