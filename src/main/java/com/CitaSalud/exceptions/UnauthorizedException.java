package com.CitaSalud.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AuthException{
    //Error 403 para falta de permisos
    public UnauthorizedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
