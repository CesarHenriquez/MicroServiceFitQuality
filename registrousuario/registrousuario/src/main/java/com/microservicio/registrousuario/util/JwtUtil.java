package com.microservicio.registrousuario.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "EstaEsMiClaveSuperSecretaParaFirmarLosJWTs1234567890";

    public Claims extractAllClaims(String token) {
        
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // Si después de limpiar el token es nulo o está vacío, regresa
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            // 2. Parsear el token usando la clave secreta
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // Manejar excepciones de token inválido, expirado o malformado
            return null;
        }
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) return null;
        // Asegura la conversión correcta de Integer (en Claims) a Long
        return Long.valueOf(claims.get("userId").toString()); 
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) return null;
        return (String) claims.get("rol");
    }
}