package com.CitaSalud.dto;

/**
 * Contrato de datos de entrada (Input DTO) específico para la mutación 'cancelarExamen' de GraphQL.
 * El ID del usuario se omite, ya que se obtendrá del token.
 */
public record CancelacionInput(
        /**
         * Identificador único de la cita (id_cita) que se desea cancelar.
         * Mapea al tipo 'ID!' en el esquema GraphQL.
         */
        Long citaId,

        /**
         * Razón o motivo por el cual el usuario cancela la cita.
         * Mapea al tipo 'String' en el esquema GraphQL.
         */
        String motivo
) {}
