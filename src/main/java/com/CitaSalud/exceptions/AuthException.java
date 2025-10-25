package com.CitaSalud.exceptions;

import org.springframework.http.HttpStatus;

public class AuthException extends RuntimeException {

    /**
     * Excepción base para todos los errores relacionados con autenticación.
     *
     * Esta es la excepción padre de la cual heredan otras excepciones más específicas
     * relacionadas con autenticación (BadCredentialsException, UnauthorizedException, etc.)
     *
     * Extiende RuntimeException, por lo que es una excepción no verificada (unchecked)
     * que no requiere declaración explícita en firmas de métodos.
     */

    private final HttpStatus status;

    public AuthException(String message, HttpStatus httpStatus) {
        super(message);
        this.status = httpStatus;
    }

    //Metodo para el DataFetcherController de GraphQL
    public HttpStatus getStatus() {
        return status;
    }

}
