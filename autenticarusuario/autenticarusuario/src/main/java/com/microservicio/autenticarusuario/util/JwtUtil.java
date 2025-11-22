package com.microservicio.autenticarusuario.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.microservicio.autenticarusuario.model.Usuario;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {
    // Clave secreta para firmar el JWT
    
    private final String SECRET_KEY = "EstaEsMiClaveSuperSecretaParaFirmarLosJWTs1234567890";

    // Tiempo de vida del token 
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 horas en milisegundos

    public String generateToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        
        //el ID y el Rol del usuario en los claims (cuerpo) del token
        claims.put("userId", usuario.getId());
        claims.put("rol", usuario.getRol().getNombre()); // Usa el nombre del rol completo: ADMINISTRADOR, CLIENTE, DELIVERY
        
        return createToken(claims, usuario.getNickname());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) 
                .setIssuedAt(new Date(System.currentTimeMillis())) // Fecha de emisión
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Fecha de expiración
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // Algoritmo y clave secreta
                .compact();
    }

}

