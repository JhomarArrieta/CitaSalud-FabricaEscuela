package com.CitaSalud.exceptions;

/**
 * Excepción de negocio lanzada cuando una entidad (recurso) esperada
 * no puede ser localizada en la base de datos basándose en su identificador.
 *
 * * Casos de uso típicos:
 * - Fallo al buscar un Usuario por ID (en el servicio 'CitaExamenService').
 * - Fallo al buscar una Sede, un Examen o cualquier otra entidad maestra.
 *
 * * Esta excepción es interceptada por el manejador de errores de GraphQL y mapeada
 * a una respuesta de cliente con clasificación NOT_FOUND (HTTP 404).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}