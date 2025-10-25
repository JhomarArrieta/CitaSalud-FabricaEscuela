package com.CitaSalud.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Componente encargado de la creación, validación y extracción de información de tokens JWT.
 *
 * Lectura de configuración:
 * - app.jwtSecret: secreto usado para firmar y verificar los tokens (debe gestionarse de forma segura).
 * - app.jwtExpirationInMs: tiempo de expiración del token en milisegundos.
 *
 * Notas de seguridad y diseño:
 * - El secreto debe tener la longitud adecuada para el algoritmo de firma seleccionado (HS512).
 *   Se recomienda almacenar el secreto en un store seguro (vault, variables de entorno protegidas, etc.).
 * - Este componente no gestiona revocación de tokens; para revocación considerar lista negra o
 *   control por versión de credenciales en la base de datos.
 * - Se usan Claims personalizados ("roles") para incluir los roles del usuario dentro del token.
 */
@Component
public class JwtTokenProvider {

    /**
     * Secreto para firmar tokens. Debe inyectarse desde configuración y tratarse como sensible.
     */
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    /**
     * Caducidad del token en milisegundos.
     */
    @Value("${app.jwtExpirationInMs}")
    private long jwtExpirationMs;

    /**
     * Clave derivada del secreto para operaciones criptográficas de firma/verificación.
     * Se inicializa en {@link #init()}.
     */
    private Key key;

    /**
     * Inicializa la clave de firma a partir del secreto configurado.
     * Se ejecuta después de la construcción del bean.
     *
     * Importante: validar que el secreto tenga la entropía/longitud necesaria para HS512.
     */
    @PostConstruct
    public void init() {
        // Keys.hmacShaKeyFor valida internamente la longitud mínima del secreto para el algoritmo HMAC.
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Genera un token JWT para un usuario identificado por su id e incluyendo sus roles.
     *
     * Estructura del token:
     * - Subject: id del usuario (como String).
     * - Claims: mapa donde se incluye la colección de roles bajo la clave "roles".
     * - IssuedAt/Expiration: tiempos de emisión y expiración.
     * - Firma: HS512 usando la clave derivada del secreto.
     *
     * @param userId id numérico del usuario que será la subject del token
     * @param roles conjunto de roles asociados al usuario que se incluirán en las claims
     * @return token JWT firmado en formato compactado (String)
     */
    public String generateToken(Long userId, Set<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);

        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Valida la integridad y formato de un token JWT.
     *
     * Comprueba firma, formato y fecha de expiración. En caso de cualquier excepción de parseo
     * o validación devuelve false, evitando propagar excepciones de librería fuera de este componente.
     *
     * @param token token JWT en formato String
     * @return true si el token es válido; false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Registrar o instrumentar la excepción en monitorización si es necesario.
            return false;
        }
    }

    /**
     * Extrae el id del usuario (subject) contenido en el token JWT.
     *
     * Asume que el token ya es válido; el llamador puede preferir invocar {@link #validateToken(String)}
     * antes de este método para evitar excepciones.
     *
     * @param token token JWT válido
     * @return id del usuario como Long
     * @throws RuntimeException si el token no es parseable o el subject no es numérico
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extrae la colección de roles almacenada en las claims del token.
     *
     * El método convierte la claim "roles" a un Set<String>. Si la claim no existe o no es una colección,
     * se devuelve un Set vacío.
     *
     * @param token token JWT válido
     * @return conjunto de roles extraídos del token; nunca es null
     */
    public Set<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        Object raw = claims.get("roles");
        if (raw instanceof Collection) {
            return ((Collection<?>) raw).stream().map(Object::toString).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }
}
