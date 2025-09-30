package com.CitaSalud.exceptions;

import org.springframework.http.HttpStatus;

public class BadCredentialsException extends AuthException {
    //Error 401 para credenciales fallidas
    public BadCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
