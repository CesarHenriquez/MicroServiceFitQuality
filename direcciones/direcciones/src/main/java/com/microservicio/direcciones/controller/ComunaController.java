package com.microservicio.direcciones.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservicio.direcciones.model.Comuna;
import com.microservicio.direcciones.service.ComunaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/comunas")
public class ComunaController {
    private final ComunaService comunaService;

    public ComunaController(ComunaService comunaService) {
        this.comunaService = comunaService;
    }

    @Operation(summary = "Listar todas las comunas", description = "Obtiene una lista de todas las comunas disponibles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de comunas obtenida correctamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        
    })
    
    @GetMapping
    public List<Comuna> listar() {
        return comunaService.listar();
    }
    @Operation(summary = "Listar comunas por región", description = "Obtiene una lista de comunas filtradas por el ID de la región")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de comunas obtenida correctamente"),
        @ApiResponse(responseCode = "404", description = "Región no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })

    @GetMapping("/region/{regionId}")
    public List<Comuna> listarPorRegion(@PathVariable Long regionId) {
        return comunaService.listarPorRegion(regionId);
    }
    @Operation(summary = "Guardar una nueva comuna", description = "Permite guardar una nueva comuna en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Comuna guardada correctamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })

    @PostMapping
    public Comuna guardar(@RequestBody Comuna comuna) {
        return comunaService.guardar(comuna);
    }


}
