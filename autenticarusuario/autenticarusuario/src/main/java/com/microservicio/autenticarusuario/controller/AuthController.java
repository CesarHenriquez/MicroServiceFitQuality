package com.microservicio.autenticarusuario.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

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

    @Operation(
        summary = "Autenticar usuario y obtener JWT", 
        description = "Permite validar un usuario y obtener un Token JWT si la autenticación es exitosa."
    )
    @ApiResponses(value = {
        // En caso de éxito, devuelve el JWT 
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa, JWT devuelto", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Credenciales del usuario (nickname y clave)", required = true,
            content = @Content(schema = @Schema(example = "{\"nickname\": \"admin1\", \"clave\": \"1234\"}"))
        )
        @RequestBody Map<String, String> credentials
    ) {
        String nickname = credentials.get("nickname");
        String clave = credentials.get("clave");

        return authService.autenticar(nickname, clave)
                .map(tokenOerror -> {
                    // Si la respuesta es un JWT (no un mensaje de error), devuelve OK.
                    if (tokenOerror.startsWith("Error") || tokenOerror.contains("inválidas") || tokenOerror.contains("encontrado")) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(tokenOerror);
                    }
                    return ResponseEntity.ok(tokenOerror);
                });
    }

}

