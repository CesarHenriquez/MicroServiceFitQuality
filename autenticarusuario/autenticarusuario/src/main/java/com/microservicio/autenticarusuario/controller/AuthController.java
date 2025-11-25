package com.microservicio.autenticarusuario.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.microservicio.autenticarusuario.dto.LoginResponseDTO;
import com.microservicio.autenticarusuario.model.Usuario;
import com.microservicio.autenticarusuario.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Autenticar usuario y obtener JWT", description = "Permite validar un usuario y obtener un Token JWT si la autenticación es exitosa.")
    @ApiResponses(value = {
            // En caso de éxito, devuelve el JWT
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa, JWT devuelto", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<?>> login(
            @RequestBody Map<String, String> credentials) {

        String email = credentials.get("correo");
        String clave = credentials.get("clave");

        return authService.autenticar(email, clave)
                .map(resultado -> {
                    if (resultado instanceof LoginResponseDTO) {
                        
                        return ResponseEntity.ok((LoginResponseDTO) resultado);
                    } else {
                        
                        String error = (String) resultado;
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
                    }
                });
    }

    @GetMapping("/users/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
       

        
        Mono<Usuario> userMono = authService.findUserByEmail(email); 

        return userMono.map(user -> ResponseEntity.ok(user))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .block(); 
    }

}