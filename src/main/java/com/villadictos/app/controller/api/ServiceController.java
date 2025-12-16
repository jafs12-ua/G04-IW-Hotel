package com.villadictos.app.controller.api;

import com.villadictos.app.dto.api.ServiceDetailDTO;
import com.villadictos.app.service.ServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Services", description = "Hotel services management endpoints")
public class ServiceController {

    private final ServicioService servicioService;

    public ServiceController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    @Operation(summary = "List all services", description = "Get a list of all hotel services")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    })
    public ResponseEntity<List<ServiceDetailDTO>> listServices() {
        List<ServiceDetailDTO> services = servicioService.findAllServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service details", description = "Get detailed information about a specific service")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service found"),
            @ApiResponse(responseCode = "404", description = "Service not found", content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorDTO")))
    })
    public ResponseEntity<ServiceDetailDTO> getServiceDetail(
            @Parameter(description = "Service ID") @PathVariable Long id) {
        ServiceDetailDTO service = servicioService.findServiceById(id);
        return ResponseEntity.ok(service);
    }
}
