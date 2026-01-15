package com.villadictos.app.repository;

import com.villadictos.app.model.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiClientRepository extends JpaRepository<ApiClient, Long> {

    Optional<ApiClient> findByApiKey(String apiKey);

    Optional<ApiClient> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByApiKey(String apiKey);
}
