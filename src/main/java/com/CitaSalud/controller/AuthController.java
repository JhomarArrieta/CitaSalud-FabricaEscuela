package com.CitaSalud.controller;

import com.CitaSalud.core.services.AuthService;
import com.CitaSalud.dto.AuthResponse;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * Controlador GraphQL encargado de las operaciones de autenticación.
 *
 * Expone mutaciones relacionadas con el inicio de sesión y delega la lógica
 * de verificación y generación de tokens al servicio {@code AuthService}.
 *
 * Responsabilidades:
 * - Recibir las solicitudes GraphQL de autenticación.
 * - Validar y transformar argumentos mínimos para el servicio de autenticación.
 * - Devolver la respuesta de autenticación (por ejemplo, token y datos del usuario).
 *
 * Observaciones de seguridad:
 * - Las credenciales deben transmitirse siempre sobre TLS/HTTPS.
 * - La verificación de contraseñas y el manejo de tokens corresponden a AuthService.
 */
@Controller
public class AuthController {

    private final AuthService authService;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param authService servicio que contiene la lógica de autenticación y generación de tokens
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Mutación GraphQL que autentica un usuario por correo y contraseña.
     *
     * Esta mutación se mapea a una operación GraphQL (ej. "login") y delega la
     * autenticación al servicio {@code AuthService}. La respuesta contiene
     * información de autenticación (por ejemplo, token JWT y datos mínimos del usuario).
     *
     * @param email correo electrónico del usuario que intenta autenticarse
     * @param contrasena contraseña en texto plano enviada desde el cliente (debe viajar por TLS)
     * @return {@link AuthResponse} objeto que encapsula el resultado de la autenticación
     */
    @MutationMapping
    public AuthResponse login(@Argument String email, @Argument String contrasena) {
        // Delegar la autenticación al servicio y retornar la respuesta correspondiente.
        AuthResponse response = authService.authenticateUser(email, contrasena);
        return response;
    }
}
