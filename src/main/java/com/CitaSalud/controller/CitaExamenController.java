package com.CitaSalud.controller;

import com.CitaSalud.core.services.CitaExamenService;
import com.CitaSalud.domain.entities.CitaExamen;
import com.CitaSalud.dto.AgendamientoDTO;
import com.CitaSalud.dto.AgendamientoInput;
import com.CitaSalud.dto.CancelacionDTO;
import com.CitaSalud.dto.CancelacionInput;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador GraphQL encargado de las operaciones relacionadas con citas de examen.
 *
 * Responsabilidades:
 * - Exponer la mutación para agendar una cita de examen.
 * - Extraer el identificador del usuario autenticado desde el contexto de seguridad
 *   (no confiar en valores enviados por el cliente).
 * - Convertir y validar el input de GraphQL hacia un DTO de servicio y delegar la
 *   lógica de negocio al servicio correspondiente.
 *
 * Notas de seguridad y diseño:
 * - El identificador del usuario se obtiene del SecurityContext para evitar suplantación.
 * - La mutación está protegida con @PreAuthorize para permitir solo usuarios con rol de paciente.
 * - Evitar lógica de negocio en el controlador; todas las validaciones complejas deben residir en el servicio.
 */
@Controller
public class CitaExamenController {

    private final CitaExamenService citaExamenService;

    public CitaExamenController(CitaExamenService citaExamenService) {
        this.citaExamenService = citaExamenService;
    }

    /**
     * Mutación GraphQL para agendar un examen.
     *
     * Flujo resumido:
     * 1. Verificar que el usuario esté autenticado y tenga el rol adecuado.
     * 2. Obtener el ID del usuario desde el SecurityContext (establecido por el filtro JWT).
     * 3. Mapear el input de GraphQL a un DTO de servicio, usando el ID seguro del usuario.
     * 4. Llamar al servicio para realizar el agendamiento y devolver la entidad resultante.
     *
     * Autorización:
     * - @PreAuthorize("hasRole('ROLE_PACIENTE')") restringe el acceso a usuarios con el rol de paciente.
     *   Nota: según la configuración de Spring Security, hasRole puede esperarse sin el prefijo "ROLE_"
     *   (ej. hasRole('PACIENTE')). Revisar convenciones del proyecto antes de modificar.
     *
     * Manejo del principal:
     * - El principal puede ser Long o String (dependiendo de cómo lo establezca el filtro JWT).
     * - El método convierte de forma segura a Long y lanza excepciones claras si el valor no es válido.
     *
     * Conversión de fecha:
     * - Se utiliza LocalDateTime.parse(...) por simplicidad; el input debe estar en formato ISO-8601
     *   (ej. "2025-10-26T15:30:00"). Manejar parse exceptions en el servicio si se necesita mayor tolerancia.
     *
     * @param input datos de agendamiento recibidos desde la mutación GraphQL
     * @return la entidad CitaExamen creada/actualizada por el servicio
     * @throws RuntimeException / IllegalArgumentException en caso de usuario no autenticado o ID inválido
     */
    @MutationMapping
    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    public CitaExamen agendarExamen(
            @Argument AgendamientoInput input
    ) {

        // Obtener la autenticación actual desde el contexto de seguridad.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Validación básica: asegurar que exista una autenticación válida.
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Acceso denegado: usuario no autenticado.");
        }

        // Extraer y normalizar el ID del usuario desde el principal.
        Long usuarioId;
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                usuarioId = Long.parseLong((String) principal);
            } else if (principal instanceof Long) {
                usuarioId = (Long) principal;
            } else {
                // Si el principal es otro tipo, señalamos el problema con claridad.
                throw new IllegalArgumentException("El principal de seguridad no contiene un ID de usuario válido.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID de usuario contenido en el token no es numérico.", e);
        }

        // Mapear el input de GraphQL al DTO de servicio, asegurando el uso del ID autenticado.
        AgendamientoDTO dto = new AgendamientoDTO();
        dto.setUsuarioId(usuarioId);
        dto.setSedeId(input.sedeId());
        dto.setExamenId(input.examenId());

        // Parseo de fecha/hora: requiere formato ISO-8601. Validaciones adicionales deben hacerse en el servicio.
        dto.setFechaHora(LocalDateTime.parse(input.fechaHora()));

        // Delegar la operación al servicio responsable y devolver el resultado.
        return citaExamenService.agendarExamen(dto);
    }

    /**
     * Mutación GraphQL para cancelar un examen previamente agendado.
     *
     * Flujo resumido:
     * 1. Verificar que el usuario esté autenticado y tenga el rol adecuado.
     * 2. Obtener el ID del usuario desde el SecurityContext.
     * 3. Mapear el input a un DTO de servicio (CancelacionDTO).
     * 4. Llamar al servicio para realizar la cancelación. El servicio validará
     * que el usuario autenticado sea el dueño de la cita.
     *
     * @param input datos de cancelación (ID de la cita y motivo)
     * @return la entidad CitaExamen actualizada (con estado "CANCELADA")
     */
    @MutationMapping
    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    public CitaExamen cancelarExamen(
            @Argument CancelacionInput input
    ) {
        // 1. Obtener el ID de usuario autenticado (Lógica de seguridad copiada de agendarExamen)
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

        // 2. Mapear al DTO de servicio
        CancelacionDTO dto = new CancelacionDTO();
        dto.setUsuarioId(usuarioId); // ID seguro del token
        dto.setCitaId(input.citaId());
        dto.setMotivo(input.motivo());

        // 3. Delegar la operación al servicio
        return citaExamenService.cancelarExamen(dto);
    }

    /**
     * Query para obtener las citas del usuario autenticado.
     * @return lista de CitaExamen
     */
    @QueryMapping
    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    public List<CitaExamen> misCitas() {
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

        return citaExamenService.obtenerCitasPorUsuario(usuarioId);
    }
}
