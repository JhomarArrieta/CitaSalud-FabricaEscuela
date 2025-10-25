package com.CitaSalud.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando las credenciales proporcionadas son inválidas.
 * 
 * Casos de uso:
 * - Email no existe en la base de datos
 * - Contraseña incorrecta
 * - Combinación email/contraseña no coincide
 * 
 * Esta excepción debe manejarse en un @ControllerAdvice para retornar
 * HTTP 401 Unauthorized al cliente.
 */

public class BadCredentialsException extends AuthException {
    //Error 401 para credenciales fallidas
    public BadCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
