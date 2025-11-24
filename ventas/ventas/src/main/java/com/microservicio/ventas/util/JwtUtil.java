package com.microservicio.ventas.util;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JwtUtil {
    
    // Clave secreta confirmada (DEBE ser idéntica a la del MS 8021)
    private final String SECRET_KEY = "EstaEsMiClaveSuperSecretaParaFirmarLosJWTs1234567890";
    
    /**
     * Extrae y valida todos los Claims del token JWT.
     * Retorna null si el token es inválido, expirado o la firma es incorrecta.
     */
    public Claims extractAllClaims(String token) {
        
        // 1. Limpieza del prefijo "Bearer "
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); 
        }
        
        // 2. Comprobación de token nulo/vacío después de la limpieza
        if (token == null || token.isBlank()) {
            return null; // Token no proporcionado
        }

        try {
            // 3. Parsear y devolver los Claims
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // Error de firma, expiración o malformación del token.
            return null;
        }
    }

    /**
     * Extrae el ID de usuario del token y lo convierte a Long de forma segura.
     * Esto resuelve el problema de que JWT pueda deserializar el ID como Integer.
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) return null;
        
        // Conversión CRÍTICA: Convertimos a String y luego a Long de forma segura.
        try {
            return Long.valueOf(String.valueOf(claims.get("userId"))); 
        } catch (NumberFormatException | NullPointerException e) {
            // Si el claim 'userId' no es un número o no existe (posible error del token original).
            return null; 
        }
    }

    /**
     * Extrae el Rol del token.
     */
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) return null;
        return (String) claims.get("rol");
    }

}