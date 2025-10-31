package com.CitaSalud.dto;

import lombok.Data;

/**
 * DTO interno usado para transportar los datos de cancelación
 * de la capa de Controller a la capa de Service.
 * Contiene el ID del usuario autenticado para validación de permisos.
 */
@Data
public class CancelacionDTO {

    /**
     * Identificador del usuario autenticado (obtenido del token).
     * Se usará para verificar que el usuario es dueño de la cita que intenta cancelar.
     */
    private Long usuarioId;

    /**
     * Identificador de la CitaExamen a cancelar.
     */
    private Long citaId;

    /**
     * Motivo de la cancelación.
     */
    private String motivo;
}
