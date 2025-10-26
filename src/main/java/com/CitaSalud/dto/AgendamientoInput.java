package com.CitaSalud.dto;

import java.time.LocalDateTime;

/**
 * Input de GraphQL para la mutación agendarExamen.
 * Contiene solo la información que el cliente puede enviar.
 * Nota: 'usuarioId' se omite por seguridad, se obtiene del JWT.
 */
public record AgendamientoInput(
        // ID! en GraphQL -> Long en Java
        Long sedeId,
        Long examenId,
        // String en GraphQL -> se parseará a LocalDateTime en el controlador
        String fechaHora
) {}