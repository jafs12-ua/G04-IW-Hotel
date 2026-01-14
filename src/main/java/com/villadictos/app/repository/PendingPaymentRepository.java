package com.villadictos.app.repository;

import com.villadictos.app.model.PendingPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingPaymentRepository extends JpaRepository<PendingPayment, Long> {
    Optional<PendingPayment> findByToken(String token);
}
