package com.CitaSalud.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    /*
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    private final SecretKey key;

    public JwtTokenProvider(@Value("${app.jwtSecret}") String jwtSecret) {
        // Genera la clave SecretKey a partir de la String de forma segura
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(long userId, String rolName) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(Long.toString(userId))              //id del usuario
                .claim("rol", rolName)                       //Rol en el cuerpo del token
                .setIssuedAt(now)                               //Hora de emision
                .setExpiration(expiryDate)                      //hora de expiracion
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
    */
    public String generateToken(Long userId, String role) {
        // Provisional, devuelve un string fijo
        return "FAKE-TOKEN-USER-" + userId + "-ROLE-" + role;
    }

}
