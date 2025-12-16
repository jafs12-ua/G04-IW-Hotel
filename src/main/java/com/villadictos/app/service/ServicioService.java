package com.villadictos.app.service;

import com.villadictos.app.dto.api.ServiceDetailDTO;
import com.villadictos.app.exception.ServiceNotFoundException;
import com.villadictos.app.model.Servicio;
import com.villadictos.app.repository.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    /**
     * Get all services
     */
    public List<ServiceDetailDTO> findAllServices() {
        return servicioRepository.findAll().stream()
                .map(ServiceDetailDTO::fromServicio)
                .toList();
    }

    /**
     * Get service by ID
     */
    public ServiceDetailDTO findServiceById(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));
        return ServiceDetailDTO.fromServicio(servicio);
    }
}
