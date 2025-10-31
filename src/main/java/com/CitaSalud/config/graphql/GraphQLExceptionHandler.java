package com.CitaSalud.config.graphql;

import com.CitaSalud.exceptions.BadCredentialsException;
import com.CitaSalud.exceptions.RecursoNoEncontradoException;
import com.CitaSalud.exceptions.CuposAgotadosException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Adaptador de Resolución de Excepciones para Spring GraphQL.
 * * Esta clase intercepta las excepciones lanzadas por los controladores y servicios
 * (Data Fetchers) y las traduce a un formato estándar de GraphQLError,
 * asegurando que el cliente reciba un código de error (Classification)
 * semánticamente correcto (ej. NOT_FOUND, BAD_REQUEST) en lugar de un genérico INTERNAL_ERROR.
 */
@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    /**
     * Método principal para resolver la excepción a un único error GraphQL.
     * @param ex Excepción lanzada por la lógica de negocio o de seguridad.
     * @param env Entorno de ejecución del Data Fetcher.
     * @return Objeto GraphQLError formateado.
     */
    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {

        // 1. Error de Credenciales (Mapeo a 401 UNAUTHORIZED)
        if (ex instanceof BadCredentialsException) {
            return buildError(ex, ErrorType.UNAUTHORIZED, env);
        }

        // 2. Error de Autorización (Mapeo a 403 FORBIDDEN - Usado por @PreAuthorize)
        if (ex instanceof AccessDeniedException) {
            // Se usa un mensaje genérico por seguridad para no revelar detalles internos
            return buildError("Acceso denegado. No tienes los permisos necesarios.", ErrorType.FORBIDDEN, env);
        }

        // 3. Error de Recurso No Encontrado (Mapeo a 404 NOT_FOUND)
        if (ex instanceof RecursoNoEncontradoException) {
            return buildError(ex, ErrorType.NOT_FOUND, env);
        }

        // 4. Error de Lógica de Negocio/Validación (Mapeo a 400 BAD_REQUEST)
        if (ex instanceof CuposAgotadosException) {
            return buildError(ex, ErrorType.BAD_REQUEST, env);
        }

        // 5. Otros errores de Runtime (Fallback a 500 INTERNAL_ERROR)
        // Solo revela el mensaje si es una RuntimeException no mapeada.
        return buildError("Error interno en el servidor: " + ex.getMessage(), ErrorType.INTERNAL_ERROR, env);
    }

    /**
     * Helper para construir un GraphQLError tomando el mensaje de la excepción Throwable.
     */
    private GraphQLError buildError(Throwable ex, ErrorType errorType, DataFetchingEnvironment env) {
        return buildError(ex.getMessage(), errorType, env);
    }

    /**
     * Construye y retorna el objeto GraphQLError con los metadatos necesarios.
     */
    private GraphQLError buildError(String message, ErrorType errorType, DataFetchingEnvironment env) {
        return GraphqlErrorBuilder.newError()
                .errorType(errorType)
                .message(message)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }
}