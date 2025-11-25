package com.CitaSalud.domain.entities;

/**
 * Representa los estados posibles de una CitaExamen.
 *
 * Utilizar un Enum en lugar de Strings previene errores de tipeo
 * y asegura que solo se puedan usar valores definidos en la lógica de negocio.
 *
 * - PENDIENTE: Pendiente de confirmación (ej. requiere pago o documentos).
 * - CONFIRMADO: Agendada y confirmada.
 * - CANCELADO: Cancelada (por paciente o sistema).
 * - REQUIERE_DOCUMENTOS: El paciente debe subir documentación.
 * - REGISTRADO_EN_SEDE: El paciente se ha registrado en la sede.
 * - COMPLETADO: La cita fue completada (examen tomado).
 * - NO_SE_PRESENTO: El paciente no se presentó.
 */
public enum EstadoCita {
    AGENDADA,
    CONFIRMADO,
    CANCELADA,
    REQUIERE_DOCUMENTOS,
    REGISTRADO_EN_SEDE,
    COMPLETADO,
    NO_SE_PRESENTO
}
