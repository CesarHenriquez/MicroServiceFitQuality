package com.microservicio.direcciones.controller;

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


import com.microservicio.direcciones.model.Comuna;
import com.microservicio.direcciones.model.Direccion;

import com.microservicio.direcciones.service.DireccionService;
import com.microservicio.direcciones.util.JwtUtil; 

@RestController
@RequestMapping("/api/direcciones")
public class DireccionController {
    private final DireccionService direccionService;
   
    private final JwtUtil jwtUtil; 

   
    public DireccionController(DireccionService direccionService, JwtUtil jwtUtil) {
        this.direccionService = direccionService;
        this.jwtUtil = jwtUtil; 
        
    }

    
    @GetMapping
    public List<Direccion> listar() {
        return direccionService.listar();
    }

    
    @GetMapping("/usuario/{usuarioId}")
    public List<Direccion> porUsuario(@PathVariable Long usuarioId) {
        return direccionService.buscarPorUsuario(usuarioId);
    }

    @PostMapping
    public ResponseEntity<?> guardar(
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

        try {
            

            
            if (!rol.equalsIgnoreCase("CLIENTE")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Acceso denegado: solo usuarios con rol CLIENTE pueden registrar direcciones.");
            }

           
            Direccion direccion = new Direccion();
            direccion.setCalle((String) payload.get("calle"));
            direccion.setCodigoPostal((String) payload.get("codigoPostal"));
            direccion.setUsuarioId(usuarioId); 

            Comuna comuna = new Comuna();
            
            if (!payload.containsKey("comunaId")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Falta el campo 'comunaId' en el cuerpo de la solicitud.");
            }
            comuna.setId(Long.valueOf(payload.get("comunaId").toString()));
            direccion.setComuna(comuna);

            Direccion guardada = direccionService.guardar(direccion);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardada);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al procesar la solicitud: " + e.getMessage());
        }
    }
}