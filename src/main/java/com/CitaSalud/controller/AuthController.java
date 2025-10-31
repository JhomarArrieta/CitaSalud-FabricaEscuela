package com.CitaSalud.controller;

import com.CitaSalud.core.services.AuthService;
import com.CitaSalud.dto.AuthResponse;
import com.CitaSalud.dto.AuthInput; 
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * Controlador GraphQL responsable de las operaciones de autenticación.
 *
 * - Expone la mutación "login" que recibe un {@link AuthInput} y devuelve un {@link AuthResponse}.
 * - Delegación clara: toda la lógica de validación y generación de tokens reside en {@link AuthService}.
 *
 * Consideraciones de seguridad:
 * - Las credenciales deben transmitirse siempre por canales seguros (HTTPS/TLS).
 * - No realizar aquí validaciones de negocio complejas; el controlador debe mantener responsabilidad mínima.
 */
@Controller
public class AuthController {

    private final AuthService authService;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param authService servicio que implementa la lógica de autenticación y creación de tokens
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Mutación GraphQL para iniciar sesión.
     *
     * Recibe un objeto {@link AuthInput} que contiene email y contraseña, delega la autenticación
     * al servicio {@link AuthService} y retorna un {@link AuthResponse} con el token JWT en caso
     * de éxito.
     *
     * Notas:
     * - No exponer mensajes de error que puedan facilitar enumeración de usuarios.
     * - El token retornado debe almacenarse y transmitirse por el cliente de forma segura.
     *
     * @param input objeto que contiene las credenciales (email, contrasena)
     * @return AuthResponse con el token JWT cuando la autenticación es exitosa
     */
    @MutationMapping
    public AuthResponse login(@Argument AuthInput input) {
        // Delegar la autenticación al servicio usando los datos del DTO/Input.
        AuthResponse response = authService.authenticateUser(
                input.getEmail(),
                input.getContrasena()
        );
        return response;
    }
}