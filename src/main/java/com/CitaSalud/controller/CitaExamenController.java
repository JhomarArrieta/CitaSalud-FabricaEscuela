package com.CitaSalud.controller;

import com.CitaSalud.core.services.CitaExamenService;
import com.CitaSalud.core.services.DisponibilidadService; // ¡Ahora usamos el servicio correcto!
import com.CitaSalud.domain.entities.CitaExamen;
import com.CitaSalud.dto.AgendamientoDTO;
import com.CitaSalud.dto.AgendamientoInput;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // Clase clave
import org.springframework.security.core.context.SecurityContextHolder; // Clase clave
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class CitaExamenController {

    private final CitaExamenService citaExamenService;

    public CitaExamenController(CitaExamenService citaExamenService) {
        this.citaExamenService = citaExamenService;
    }

    /**
     * Mutación para agendar un examen.
     * Requiere que el usuario esté autenticado y tenga el rol 'ROLE_PACIENTE'.
     * El ID del usuario se extrae del contexto de seguridad, no del input.
     */
    @MutationMapping
    @PreAuthorize("hasRole('ROLE_PACIENTE')") // ¡Spring Security manejará la autorización basada en los roles del contexto!
    public CitaExamen agendarExamen(
            @Argument AgendamientoInput input
    ) {

        // 1. Obtener la autenticación del contexto de seguridad
        // La autenticación fue establecida por JwtAuthenticationFilter.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Extraer el ID del usuario del principal
        // En tu JwtAuthenticationFilter, el principal es el Long userId
        if (authentication == null || authentication.getPrincipal() == null) {
            // Esto no debería pasar si @PreAuthorize funciona, pero es una buena práctica de seguridad
            throw new RuntimeException("Acceso denegado: Usuario no autenticado.");
        }

        // ¡Casteamos el principal directamente a Long!
        // Esto funciona porque el filtro lo estableció así:
        // new UsernamePasswordAuthenticationToken(userId, null, authorities);
        Long usuarioId;
        try {
            // Manejar si el principal se configuró como String (subject del JWT) en lugar de Long
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                usuarioId = Long.parseLong((String) principal);
            } else if (principal instanceof Long) {
                usuarioId = (Long) principal;
            } else {
                throw new IllegalArgumentException("El principal de seguridad no es un ID válido.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID de usuario en el token no es numérico.");
        }


        // 3. Convertir el Input de GraphQL a nuestro DTO de servicio
        AgendamientoDTO dto = new AgendamientoDTO();

        // 4. ¡Usamos el ID del usuario autenticado y seguro!
        dto.setUsuarioId(usuarioId);

        // 5. Usamos los datos del input
        dto.setSedeId(input.sedeId());
        dto.setExamenId(input.examenId());
        dto.setFechaHora(LocalDateTime.parse(input.fechaHora()));

        // 6. Llamamos al servicio con el DTO seguro
        return citaExamenService.agendarExamen(dto);
    }
}
