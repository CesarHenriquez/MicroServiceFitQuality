package com.microservicio.autenticarusuario.service;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.microservicio.autenticarusuario.client.UsuarioClient;
import com.microservicio.autenticarusuario.util.JwtUtil;

import reactor.core.publisher.Mono;

@Service
public class AuthService {
    private final UsuarioClient usuarioClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil; 

    public AuthService(UsuarioClient usuarioClient, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioClient = usuarioClient;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil; 
    }

    // El Mono devolverá el JWT si la autenticación es exitosa, o un mensaje de error.
    public Mono<String> autenticar(String nickname, String clave) {
        return usuarioClient.obtenerUsuarioPorNickname(nickname)
                .flatMap(usuario -> {
                    if (usuario.getRol() == null) {
                        return Mono.just("Error: el usuario no tiene rol asignado.");
                    }
                    if (clave == null || usuario.getClave() == null) {
                        return Mono.just("Error: falta la clave.");
                    }
                    if (passwordEncoder.matches(clave, usuario.getClave())) {
                        //Generación del JWT en caso de éxito 
                        String token = jwtUtil.generateToken(usuario); 
                        return Mono.just(token); 
                    } else {
                        return Mono.just("Credenciales inválidas.");
                    }
                })
                .defaultIfEmpty("Usuario no encontrado.")
                .onErrorResume(e -> Mono.just("Error al autenticar: " + e.getMessage()));
    }


}

