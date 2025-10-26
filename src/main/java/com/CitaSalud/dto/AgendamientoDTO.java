package com.CitaSalud.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO interno usado para transportar los datos de agendamiento
 * de la capa de Controller a la capa de Service.
 * * Este contrato de servicio contiene toda la información necesaria para
 * procesar el agendamiento, incluyendo el ID del usuario ya autenticado.
 */
@Data
public class AgendamientoDTO {

    /**
     * Identificador del usuario autenticado.
     * Se inyecta de forma segura desde el contexto de Spring Security (JWT)
     * en la capa de Controller para prevenir suplantación.
     */
    private Long usuarioId;

    /**
     * Identificador único de la sede seleccionada para la cita.
     */
    private Long sedeId;

    /**
     * Identificador único del examen seleccionado.
     */
    private Long examenId;

    /**
     * Fecha y hora exacta de la cita, incluyendo hora de inicio.
     * Debe estar en formato ISO 8601 (LocalDateTime).
     */
    private LocalDateTime fechaHora;
}
