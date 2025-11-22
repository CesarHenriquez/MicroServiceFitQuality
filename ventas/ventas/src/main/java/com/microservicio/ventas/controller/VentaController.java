package com.microservicio.ventas.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.microservicio.ventas.dto.ProductoDetalleDTO;
import com.microservicio.ventas.model.DetalleVenta;
import com.microservicio.ventas.model.Venta;
import com.microservicio.ventas.service.VentaService;
import com.microservicio.ventas.util.JwtUtil; 

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {
    private final VentaService ventaService;
    private final JwtUtil jwtUtil; 

    public VentaController(VentaService ventaService, JwtUtil jwtUtil) {
        this.ventaService = ventaService;
        this.jwtUtil = jwtUtil; 
    }

    @Operation(summary = "Registrar una nueva venta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venta registrada exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado, solo clientes"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })

    @PostMapping
    public ResponseEntity<?> registrarVenta(
        @RequestBody Map<String, Object> payload,
        @RequestHeader("Authorization") String authorizationHeader 
    ) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no proporcionado o formato inválido.");
        }

        Long usuarioId = jwtUtil.extractUserId(authorizationHeader);
        String rol = jwtUtil.extractRole(authorizationHeader);
        
        if (usuarioId == null || rol == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT inválido o expirado.");
        }

        
        return ventaService.registrarVenta(usuarioId, rol, payload); 
    }

    

    @Operation(summary = "Listar todas las ventas")
    @GetMapping
    public List<Venta> listarVentas() {
        return ventaService.listarVentas();
    }

    @Operation(summary = "Listar todos los detalles de ventas")
    @GetMapping("/detalles")
    public List<DetalleVenta> listarDetalles() {
        return ventaService.listarDetalles();
    }

    @Operation(summary = "Listar ventas por ID de usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ventas del usuario listadas exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "No se encontraron ventas para el usuario")
    })

    @GetMapping("/usuario/{id}")
    public ResponseEntity<?> listarPorUsuario(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader 
            ) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT no proporcionado o formato inválido.");
        }

        Long usuarioId = jwtUtil.extractUserId(authorizationHeader);
        String rol = jwtUtil.extractRole(authorizationHeader);

        if (usuarioId == null || rol == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token JWT inválido o expirado.");
        }

        try {
            
            String rolUpperCase = rol.toUpperCase();
            List<String> rolesPermitidos = Arrays.asList("ADMINISTRADOR", "DELIVERY");

            if (!rolesPermitidos.contains(rolUpperCase)) {
                
                if (!usuarioId.equals(id)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Solo ADMINISTRADOR y DELIVERY pueden ver las ventas de otros usuarios.");
                }
            }

            List<Venta> ventas = ventaService.listarPorUsuarioId(id);
            if (ventas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No se encontraron ventas para el usuario con ID: " + id);
            }
            return ResponseEntity.ok(ventas);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la solicitud: " + e.getMessage());
        }
    }

    
    
    @Operation(summary = "Listar ventas por ID de dirección")
    @GetMapping("/direccion/{id}")
    public List<Venta> listarPorDireccion(@PathVariable Long id) {
        return ventaService.listarPorDireccionId(id);
    }

    @Operation(summary = "Obtener productos de una venta por ID")
    @GetMapping("/{id}/productos")
    public ResponseEntity<?> obtenerProductosDeVenta(@PathVariable Long id) {
        try {
            List<ProductoDetalleDTO> productos = ventaService.obtenerProductosPorVentaId(id);
            if (productos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No se encontraron productos para la venta con ID: " + id);
            }
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener productos de la venta: " + e.getMessage());
        }
    }
}