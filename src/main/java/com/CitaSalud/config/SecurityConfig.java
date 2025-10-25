package com.CitaSalud.config;


import com.CitaSalud.security.CustomUserDetailsService;
import com.CitaSalud.security.JwtAuthenticationFilter;
import com.CitaSalud.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Configuración de seguridad de la aplicación.
 *
 * Esta clase define los beans y la cadena de filtros (SecurityFilterChain) usados por Spring Security.
 * - Proporciona AuthenticationManager para autenticaciones programáticas.
 * - Expone un PasswordEncoder (BCrypt) para hashing de contraseñas.
 * - Configura seguridad HTTP: deshabilita CSRF, establece sesión sin estado y añade filtro JWT.
 *
 * Responsabilidades principales:
 *  - Permitir endpoints públicos (/auth/** y /graphql).
 *  - Proteger el resto de endpoints requiriendo autenticación.
 *  - Integrar JwtAuthenticationFilter antes del filtro de usuario/contraseña estándar.
 *
 * Observaciones:
 *  - La política de sesión es STATELESS: toda la autenticación depende de tokens JWT.
 *  - CustomUserDetailsService se utiliza para cargar detalles del usuario desde la base de datos.
 */
@Configuration
public class SecurityConfig {

    /**
     * Proveedor de tokens JWT utilizado por el filtro de autenticación.
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Servicio que carga detalles de usuario (UserDetails) desde el repositorio/app.
     */
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param jwtTokenProvider proveedor responsable de validar/crear tokens JWT
     * @param customUserDetailsService servicio para cargar datos del usuario por nombre/ID
     */
    public SecurityConfig(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Expone el AuthenticationManager como bean para que pueda inyectarse donde se requiera
     * (por ejemplo, en controladores o servicios que realicen autenticación programática).
     *
     * @param configuration proveedor de la configuración de autenticación de Spring
     * @return AuthenticationManager configurado por el framework
     * @throws Exception en caso de error al obtener el AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * PasswordEncoder usado para hashear y verificar contraseñas.
     *
     * Se utiliza BCrypt por su resistencia frente a ataques de fuerza bruta y por ser el
     * estándar en aplicaciones Spring Boot.
     *
     * @return instancia de BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura la cadena de filtros de Spring Security.
     *
     * Comportamiento principal:
     *  - CSRF deshabilitado (API REST con JWT).
     *  - Sesiones sin estado (STATELESS): no se mantienen sesiones HTTP en el servidor.
     *  - Endpoints públicos: /auth/** y /graphql.
     *  - Resto de endpoints requieren autenticación.
     *  - Añade JwtAuthenticationFilter antes del UsernamePasswordAuthenticationFilter para
     *    procesar el token JWT y poblar el contexto de seguridad.
     *
     * @param http objeto HttpSecurity para configurar seguridad HTTP
     * @return SecurityFilterChain construido
     * @throws Exception en caso de error durante la construcción de la configuración de seguridad
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Filtro que valida y procesa el token JWT en las solicitudes entrantes.
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService);

        http
                // APIs REST con JWT: deshabilitar CSRF porque no se usan cookies de sesión
                .csrf(csrf -> csrf.disable())
                // No crear ni usar sesiones HTTP: cada petición debe llevar sus credenciales (token)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Configuración de autorización de rutas
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos para autenticación y GraphQL (ajustar según necesidad)
                        .requestMatchers("/auth/**", "/graphql").permitAll()
                        // Todas las demás rutas requieren autenticación
                        .anyRequest().authenticated()
                )
                // Insertar el filtro JWT antes del filtro estándar de autenticación por formulario
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}