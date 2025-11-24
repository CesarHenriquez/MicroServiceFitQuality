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
            @RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token JWT no proporcionado o formato inválido.");
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

    // ⬇️ ENDPOINT SIMPLIFICADO: NO REQUIERE TOKEN PARA LECTURA DEL HISTORIAL DE COMPRAS ⬇️
    @Operation(summary = "Listar ventas por ID de usuario (Acceso simplificado)", 
               description = "Permite a la aplicación móvil obtener el historial de compras por ID de usuario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ventas del usuario listadas exitosamente"),
            @ApiResponse(responseCode = "404", description = "No se encontraron ventas para el usuario")
    })
    @GetMapping("/usuario/{id}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable Long id) { // ⬅️ Eliminamos @RequestHeader

        try {
            // Buscamos las ventas directamente con el ID proporcionado por la App Móvil
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
    
    // ⬇️ ENDPOINT DE DELIVERY: MANTIENE LA SEGURIDAD CON TOKEN ⬇️
    @Operation(summary = "Subir comprobante de entrega", description = "Permite al DELIVERY subir la URI del comprobante y marcar la venta como entregada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comprobante guardado y venta marcada como entregada"),
            @ApiResponse(responseCode = "401", description = "Token JWT inválido"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado, solo DELIVERY"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    @PutMapping("/{id}/proof")
    public ResponseEntity<?> setDeliveryProof(
            @PathVariable Long id, 
            @RequestBody Map<String, String> payload, 
            @RequestHeader("Authorization") String authorizationHeader) {
        
        // 1. Validación de Token/Rol (DELIVERY o ADMINISTRADOR)
        String rol = jwtUtil.extractRole(authorizationHeader);
        if (rol == null || (!rol.equalsIgnoreCase("DELIVERY") && !rol.equalsIgnoreCase("ADMINISTRADOR"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: solo DELIVERY o ADMINISTRADOR.");
        }

        String proofUri = payload.get("proofUri");
        if (proofUri == null || proofUri.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El campo 'proofUri' es obligatorio.");
        }

        try {
            // 2. Llamar al servicio para actualizar
            Venta ventaActualizada = ventaService.setProofAndMarkDelivered(id, proofUri);

            if (ventaActualizada != null) {
                return ResponseEntity.ok(ventaActualizada);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Venta no encontrada con ID: " + id);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar el comprobante: " + e.getMessage());
        }
    }
}