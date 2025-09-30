package com.CitaSalud.exceptions;

import org.springframework.http.HttpStatus;

public class AuthException extends RuntimeException {

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
