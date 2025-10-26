package com.CitaSalud.dto;

import java.time.LocalDateTime;

/**
 * Contrato de datos de entrada (Input DTO) específico para la mutación 'agendarExamen' de GraphQL.
 * * Este 'record' define el set mínimo y seguro de datos que un cliente
 * puede proveer para solicitar una cita. El ID del usuario es intencionalmente
 * omitido para ser inyectado de manera segura desde el token JWT.
 */
public record AgendamientoInput(
        /**
         * Identificador único de la sede donde se desea agendar la cita.
         * Mapea al tipo 'ID!' en el esquema GraphQL.
         */
        Long sedeId,
        /**
         * Identificador único del examen que se desea agendar.
         * Mapea al tipo 'ID!' en el esquema GraphQL.
         */
        Long examenId,
        /**
         * La fecha y hora de inicio de la cita en formato String.
         * Debe ser un String ISO 8601 (YYYY-MM-DDTHH:MM:SS)
         * para ser parseado como LocalDateTime en la capa de Controller.
         */
        String fechaHora
) {}