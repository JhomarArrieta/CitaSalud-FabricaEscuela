package com.CitaSalud.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando un usuario intenta acceder a un recurso sin autorización.
 * 
 * Diferencia con BadCredentialsException:
 * - BadCredentials: credenciales incorrectas durante el login
 * - Unauthorized: usuario autenticado pero sin permisos para el recurso
 * 
 * Casos de uso:
 * - Token JWT expirado o inválido
 * - Usuario sin el rol requerido para acceder al recurso
 * - Usuario intenta acceder a datos de otro usuario
 * 
 * Esta excepción debe manejarse en un @ControllerAdvice para retornar
 * HTTP 403 Forbidden al cliente.
 */
public class UnauthorizedException extends AuthException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
