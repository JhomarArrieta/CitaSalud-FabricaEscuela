package com.CitaSalud.controller;

import com.CitaSalud.core.services.UsuarioService;
import com.CitaSalud.domain.entities.Usuario;
import com.CitaSalud.dto.ActualizarPerfilInput;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

/**
 * Controlador GraphQL para la gestión del perfil de usuario (HU-003).
 */
@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Query para obtener el perfil del usuario autenticado (HU-003).
     */
    @QueryMapping
    @PreAuthorize("hasRole('ROLE_PACIENTE')") // Protegido, solo el paciente puede ver su perfil
    public Usuario miPerfil() {
        Long usuarioId = getUsuarioIdAutenticado();
        return usuarioService.getPerfil(usuarioId);
    }

    /**
     * Mutation para actualizar el perfil del usuario autenticado (HU-003).
     */
    @MutationMapping
    @PreAuthorize("hasRole('ROLE_PACIENTE')") // Protegido
    public Usuario actualizarPerfil(@Argument ActualizarPerfilInput input) {
        Long usuarioId = getUsuarioIdAutenticado();
        return usuarioService.actualizarPerfil(usuarioId, input);
    }

    /**
     * Método de utilidad para obtener el ID del usuario del token JWT.
     * (Copiado de CitaExamenController para asegurar consistencia)
     */
    private Long getUsuarioIdAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Acceso denegado: usuario no autenticado.");
        }

        Long usuarioId;
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                usuarioId = Long.parseLong((String) principal);
            } else if (principal instanceof Long) {
                usuarioId = (Long) principal;
            } else {
                throw new IllegalArgumentException("El principal de seguridad no contiene un ID de usuario válido.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID de usuario contenido en el token no es numérico.", e);
        }
        return usuarioId;
    }
}
