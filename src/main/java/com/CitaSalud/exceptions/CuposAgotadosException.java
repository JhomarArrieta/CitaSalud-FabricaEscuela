package com.CitaSalud.exceptions;

/**
 * Excepción de negocio lanzada cuando una operación viola las reglas de capacidad o disponibilidad.
 * * Casos de uso típicos:
 * - Intentar agendar una cita cuando todos los cupos para ese slot (fecha/hora/recurso) están ocupados.
 * - Intentar decrementar el contador de disponibilidad que ya está en cero.
 * * Esta excepción es interceptada por el manejador de errores de GraphQL y mapeada
 * a una respuesta de cliente con clasificación BAD_REQUEST (HTTP 400).
 */
public class CuposAgotadosException extends RuntimeException {

    public CuposAgotadosException(String message) {
        super(message);
    }
}