package com.CitaSalud.dto;

/**
 * DTO que representa la respuesta de una operación de autenticación.
 *
 * Contiene el token JWT que debe ser retornado al cliente tras un inicio de sesión
 * exitoso. Este objeto se mantiene intencionalmente simple (solo token) para evitar
 * exponer información sensible; se puede extender en el futuro para incluir:
 * - idUsuario, roles, expiración, tipo de token, etc.
 */
public class AuthResponse {

    /**
     * Token JWT emitido por el servidor para el usuario autenticado.
     */
    private String token;

    /**
     * Constructor por defecto requerido por frameworks de deserialización.
     */
    public AuthResponse() {
    }

    /**
     * Constructor principal.
     *
     * @param token JWT a entregar al cliente
     */
    public AuthResponse(String token) {
        this.token = token;
    }

    /**
     * Obtiene el token JWT.
     *
     * @return token JWT en formato String
     */
    public String getToken() {
        return token;
    }
}
