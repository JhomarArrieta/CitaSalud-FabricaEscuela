package com.CitaSalud.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Filtro de autenticación basado en JWT que se ejecuta una vez por petición.
 *
 * Función:
 * - Extraer el token JWT del encabezado Authorization (esquema "Bearer").
 * - Validar el token mediante {@link JwtTokenProvider}.
 * - Si el token es válido, poblar el contexto de seguridad de Spring con una
 *   autenticación basada en el id del usuario y sus roles.
 *
 * Consideraciones de diseño:
 * - Este filtro no consulta la base de datos para construir un UserDetails completo;
 *   utiliza la información contenida en el token (userId y roles). Si se requiere
 *   información adicional del usuario, se puede cargar desde {@link CustomUserDetailsService}.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Proveedor encargado de validar y extraer datos del token JWT.
     */
    private final JwtTokenProvider tokenProvider;

    /**
     * Servicio para cargar detalles del usuario (opcional, inyectado para casos en que se requiera).
     */
    private final CustomUserDetailsService userDetailsService;

    /**
     * Constructor con dependencias necesarias.
     *
     * @param tokenProvider proveedor para validar y leer el token JWT
     * @param userDetailsService servicio para obtener detalles adicionales del usuario (puede no usarse en todos los flujos)
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Operación principal del filtro.
     *
     * Flujo:
     * 1. Leer encabezado Authorization y extraer token si usa esquema "Bearer".
     * 2. Validar el token mediante JwtTokenProvider.
     * 3. Extraer userId y roles desde el token.
     * 4. Construir una autenticación (UsernamePasswordAuthenticationToken) con las autoridades
     *    derivadas de los roles y establecerla en el SecurityContext.
     * 5. Continuar la cadena de filtros.
     *
     * Notas de seguridad:
     * - El encabezado Authorization debe viajar siempre sobre HTTPS.
     * - Evitar incluir información sensible en el token; validar su expiración y firma en JwtTokenProvider.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Obtener el encabezado Authorization
        String header = request.getHeader("Authorization");
        String token = null;

        // Verificar que el encabezado contenga texto y siga el esquema "Bearer <token>"
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        // Si existe token y es válido, recuperar información y poblar el contexto de seguridad
        if (token != null && tokenProvider.validateToken(token)) {
            // Extraer id del usuario desde el token
            Long userId = tokenProvider.getUserIdFromToken(token);

            // Extraer roles desde el token y convertirlos a GrantedAuthority
            Set<String> roles = tokenProvider.getRolesFromToken(token);
            var authorities = roles.stream()
                    .map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r))
                    .collect(Collectors.toList());

            // Construir una autenticación basada en el id del usuario y sus autoridades.
            // El segundo parámetro (credentials) se deja nulo porque la autenticación ya fue verificada por el token.
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);

            // Adjuntar detalles de la solicitud (IP, sessionId, etc.)
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Establecer la autenticación en el contexto de seguridad para que esté disponible en la petición
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // Continuar la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
