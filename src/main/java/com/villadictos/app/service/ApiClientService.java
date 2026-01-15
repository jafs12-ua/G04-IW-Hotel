package com.villadictos.app.service;

import com.villadictos.app.model.ApiClient;
import com.villadictos.app.repository.ApiClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ApiClientService {

    private final ApiClientRepository apiClientRepository;

    public ApiClientService(ApiClientRepository apiClientRepository) {
        this.apiClientRepository = apiClientRepository;
    }

    public List<ApiClient> findAll() {
        return apiClientRepository.findAll();
    }

    public Optional<ApiClient> findById(Long id) {
        return apiClientRepository.findById(id);
    }

    public Optional<ApiClient> findByApiKey(String apiKey) {
        return apiClientRepository.findByApiKey(apiKey);
    }

    public boolean existsByEmail(String email) {
        return apiClientRepository.existsByEmail(email);
    }

    /**
     * Valida una API Key y registra su uso
     */
    @Transactional
    public boolean validateAndRegisterUse(String apiKey) {
        Optional<ApiClient> client = apiClientRepository.findByApiKey(apiKey);
        if (client.isPresent() && client.get().isActivo()) {
            client.get().registrarUso();
            apiClientRepository.save(client.get());
            return true;
        }
        return false;
    }

    /**
     * Crea un nuevo cliente API
     */
    @Transactional
    public ApiClient createClient(String nombre, String empresa, String email, String descripcion) {
        if (apiClientRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un cliente con ese email");
        }

        ApiClient client = new ApiClient(nombre, empresa, email, descripcion);
        return apiClientRepository.save(client);
    }

    /**
     * Activa o desactiva un cliente
     */
    @Transactional
    public ApiClient toggleActive(Long id) {
        ApiClient client = apiClientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        client.setActivo(!client.isActivo());
        return apiClientRepository.save(client);
    }

    /**
     * Regenera la API Key de un cliente
     */
    @Transactional
    public ApiClient regenerateApiKey(Long id) {
        ApiClient client = apiClientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        client.regenerateApiKey();
        return apiClientRepository.save(client);
    }

    /**
     * Elimina un cliente
     */
    @Transactional
    public void deleteClient(Long id) {
        apiClientRepository.deleteById(id);
    }
}
