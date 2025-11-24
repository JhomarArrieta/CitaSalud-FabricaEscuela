package com.CitaSalud.dto;

import lombok.Data;

/**
 * DTO (Input) para la mutación de actualizar el perfil.
 * Contiene solo los campos que el usuario puede modificar.
 * Los campos coinciden con el 'input ActualizarPerfilInput' en 'schema.graphqls'.
 */
@Data
public class ActualizarPerfilInput {

    private String nombre;
    private String apellido;
    private String tipoDocumento; // Es un String, basado en tu entidad
    private String numeroDocumento;
    private String telefono;
    private String email;
}
