package com.CitaSalud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contrato de datos de entrada (Input DTO) para la mutación 'login' de GraphQL.
 * Encapsula las credenciales requeridas por el usuario para iniciar sesión.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthInput {

    /**
     * Correo electrónico del usuario, utilizado como nombre de usuario para la autenticación.
     */
    private String email;

    /**
     * Contraseña del usuario en texto plano.
     * Por requerimientos de seguridad, esta información DEBE ser transmitida
     * a través de una conexión cifrada (TLS/HTTPS).
     */
    private String contrasena;
}