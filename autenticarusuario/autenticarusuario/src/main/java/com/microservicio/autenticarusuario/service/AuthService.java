package com.microservicio.autenticarusuario.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.microservicio.autenticarusuario.client.UsuarioClient;
import com.microservicio.autenticarusuario.dto.LoginResponseDTO;
import com.microservicio.autenticarusuario.model.Usuario;
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

   
    public Mono<Object> autenticar(String email, String clave) {
        return usuarioClient.obtenerUsuarioPorEmail(email)
                .flatMap(usuario -> {
                  

                    if (passwordEncoder.matches(clave, usuario.getClave())) {
                    
                        String token = jwtUtil.generateToken(usuario);

                   
                        return Mono.just(new LoginResponseDTO(token, usuario));
                    } else {
                        return Mono.just("Contraseña incorrecta.");
                    }
                })
            
                .defaultIfEmpty("El email no está registrado.")
                .onErrorResume(e -> Mono.just("Error al autenticar: " + e.getMessage()));
    }

    public Mono<Usuario> findUserByEmail(String email) {
       
        return usuarioClient.obtenerUsuarioPorEmail(email);
    }

}
