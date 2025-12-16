package com.villadictos.app.controller.api;

import com.villadictos.app.dto.api.FacilityDetailDTO;
import com.villadictos.app.service.FacilityService;
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
@RequestMapping("/api/v1/facilities")
@Tag(name = "Facilities", description = "Facility/venue management endpoints")
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping
    @Operation(summary = "List all facilities", description = "Get a list of all facilities with optional capacity filter")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    })
    public ResponseEntity<List<FacilityDetailDTO>> listFacilities(
            @Parameter(description = "Minimum capacity filter") @RequestParam(required = false) Integer minCapacity) {
        List<FacilityDetailDTO> facilities = facilityService.findAllFacilities(minCapacity);
        return ResponseEntity.ok(facilities);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get facility details", description = "Get detailed information about a specific facility")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Facility found"),
            @ApiResponse(responseCode = "404", description = "Facility not found", content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorDTO")))
    })
    public ResponseEntity<FacilityDetailDTO> getFacilityDetail(
            @Parameter(description = "Facility ID") @PathVariable Long id) {
        FacilityDetailDTO facility = facilityService.findFacilityById(id);
        return ResponseEntity.ok(facility);
    }
}
