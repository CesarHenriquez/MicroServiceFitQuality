package com.microservicio.ventas.util;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JwtUtil {
    
    private final String SECRET_KEY = "EstaEsMiClaveSuperSecretaParaFirmarLosJWTs1234567890";

   //valida y decodifica el token JWT
    public Claims extractAllClaims(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); 
        }
        
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // El token expiró, la firma es incorrecta, o está malformado.
            return null;
        }
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) return null;
        // userId es guardado como Long en el token.
        return Long.valueOf(claims.get("userId").toString()); 
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) return null;
        return (String) claims.get("rol");
    }

}
