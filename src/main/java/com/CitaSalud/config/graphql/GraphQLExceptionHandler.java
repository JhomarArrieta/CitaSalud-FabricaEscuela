package com.CitaSalud.config.graphql;

import com.CitaSalud.exceptions.BadCredentialsException;
import com.CitaSalud.exceptions.RecursoNoEncontradoException;
import com.CitaSalud.exceptions.CuposAgotadosException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException; // Importante para @PreAuthorize
import org.springframework.stereotype.Component;


@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {

        // 1. Error de Credenciales (Login fallido - 401)
        if (ex instanceof BadCredentialsException) {
            return buildError(ex, ErrorType.UNAUTHORIZED, env);
        }

        // 2. Error de Autorización (@PreAuthorize falló - 403)
        if (ex instanceof AccessDeniedException) {
            return buildError("Acceso denegado. No tienes los permisos necesarios.", ErrorType.FORBIDDEN, env);
        }

        // 3. Error de Recurso No Encontrado (Usuario no existe - 404)
        if (ex instanceof RecursoNoEncontradoException) {
            return buildError(ex, ErrorType.NOT_FOUND, env);
        }

        // 4. Error de Lógica de Negocio (No hay cupos - 400)
        if (ex instanceof CuposAgotadosException) {
            return buildError(ex, ErrorType.BAD_REQUEST, env);
        }

        // 5. Otros errores de Runtime (ej. NullPointerException - 500)
        return buildError("Error interno: " + ex.getMessage(), ErrorType.INTERNAL_ERROR, env);
    }

    /**
     * Método helper para construir un GraphQLError estándar.
     */
    private GraphQLError buildError(Throwable ex, ErrorType errorType, DataFetchingEnvironment env) {
        return buildError(ex.getMessage(), errorType, env);
    }

    private GraphQLError buildError(String message, ErrorType errorType, DataFetchingEnvironment env) {
        return GraphqlErrorBuilder.newError()
                .errorType(errorType)
                .message(message)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }
}