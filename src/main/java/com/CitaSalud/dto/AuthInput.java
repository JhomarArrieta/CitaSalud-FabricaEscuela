package com.CitaSalud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthInput {
    /**
     * Correo electrónico del usuario.
     */
    private String email;

    /**
     * Contraseña en texto plano (debe viajar por TLS/HTTPS).
     */
    private String contrasena;
}
