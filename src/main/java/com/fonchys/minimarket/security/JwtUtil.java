package com.fonchys.minimarket.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Genera y valida los JWT usados para autenticar la API REST ({@code /api/**}).
 */
@Component
public class JwtUtil {

    private final Key signingKey;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generarToken(String email, String rol) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);
        return Jwts.builder()
            .setSubject(email)
            .claim("rol", rol)
            .setIssuedAt(ahora)
            .setExpiration(expiracion)
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    /** Lanza {@link io.jsonwebtoken.JwtException} si el token es invalido, esta expirado o fue alterado. */
    public Claims validarYObtenerClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public String obtenerEmail(String token) {
        return validarYObtenerClaims(token).getSubject();
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
