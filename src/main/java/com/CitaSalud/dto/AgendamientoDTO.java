package com.CitaSalud.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO interno usado para transportar los datos de agendamiento
 * de la capa de Controller a la capa de Service.
 * * Contiene el 'usuarioId' ya validado y extraído de forma segura
 * del contexto de Spring Security.
 */
@Data
public class AgendamientoDTO {

    // Este ID es SETEADO por el controlador desde el JWT
    private Long usuarioId;

    private Long sedeId;
    private Long examenId;
    private LocalDateTime fechaHora;
}
