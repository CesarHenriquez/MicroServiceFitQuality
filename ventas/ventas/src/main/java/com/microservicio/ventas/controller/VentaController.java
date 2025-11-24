package com.microservicio.ventas.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final JwtUtil jwtUtil; // Se mantiene por si Delivery lo usa, aunque no es crítico aquí.

    public VentaController(VentaService ventaService, JwtUtil jwtUtil) {
        this.ventaService = ventaService;
        this.jwtUtil = jwtUtil;
    }

    // ⬇️ MÉTODO MODIFICADO: SIN TOKEN, TOMA EL ID DEL JSON ⬇️
    @Operation(summary = "Registrar una nueva venta (Sin Token)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venta registrada exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> registrarVenta(@RequestBody Map<String, Object> payload) {
        try {
            // 1. Validar que venga el usuarioId en el cuerpo
            if (!payload.containsKey("usuarioId")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Falta el campo 'usuarioId' en el JSON.");
            }

            // 2. Extraer ID directamente del JSON (Android lo envía como 'usuarioId')
            Long usuarioId = Long.valueOf(payload.get("usuarioId").toString());

            // 3. Asumimos rol CLIENTE por defecto para simplificar
            String rol = "CLIENTE";

            return ventaService.registrarVenta(usuarioId, rol, payload);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la venta: " + e.getMessage());
        }
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

    // Método simplificado de historial (ya lo tenías)
    @GetMapping("/usuario/{id}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable Long id) {
        try {
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

    @GetMapping("/direccion/{id}")
    public List<Venta> listarPorDireccion(@PathVariable Long id) {
        return ventaService.listarPorDireccionId(id);
    }

    @GetMapping("/{id}/productos")
    public ResponseEntity<?> obtenerProductosDeVenta(@PathVariable Long id) {
        try {
            List<ProductoDetalleDTO> productos = ventaService.obtenerProductosPorVentaId(id);
            if (productos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontraron productos.");
            }
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // Endpoint de Delivery (Se mantiene igual o se puede simplificar si quieres)
    @PutMapping("/{id}/proof")
    public ResponseEntity<?> setDeliveryProof(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        // Simplificado: Sin validación de token para evitar problemas
        String proofUri = payload.get("proofUri");
        if (proofUri == null || proofUri.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Falta 'proofUri'.");
        }

        try {
            Venta ventaActualizada = ventaService.setProofAndMarkDelivered(id, proofUri);
            if (ventaActualizada != null) {
                return ResponseEntity.ok(ventaActualizada);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Venta no encontrada.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}