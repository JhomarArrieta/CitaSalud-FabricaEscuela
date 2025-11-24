package com.CitaSalud.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una cita médica agendada por un usuario.
 * 
 * Mapea la tabla "cita_examen" y almacena información sobre:
 * - Usuario que agenda la cita
 * - Disponibilidad (franja horaria) reservada
 * - Fecha y hora específica de la cita
 * - Estado actual y motivo de cancelación (si aplica)
 * - Auditoría de creación y modificación
 * 
 * Relaciones:
 * - ManyToOne con Usuario: múltiples citas pueden pertenecer a un usuario
 * - ManyToOne con Disponibilidad: múltiples citas pueden compartir una franja horaria
 * 
 * Se utiliza FetchType.LAZY en las relaciones para optimizar el rendimiento,
 * cargando entidades relacionadas solo cuando se acceden explícitamente.
 */
@Data
@Entity
@Table(name = "cita_examen")
public class CitaExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long idCita;

    /**
     * Usuario que solicita y agenda la cita.
     * Relación obligatoria con carga diferida.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /**
     * Disponibilidad asociada a esta cita.
     * Representa la franja horaria específica reservada para el examen.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "disponibilidad", nullable = false)
    private Disponibilidad disponibilidad;

    /**
     * Fecha y hora exacta en que se realizará el examen.
     */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /**
     * Estado actual de la cita (Agendada, Cancelada, etc.).
     * ---- MODIFICADO POR LA HISTORIA DE USUARIO ----
     * Se usa un Enum para estandarizar los estados.
     * EnumType.STRING guarda el nombre (ej. "CONFIRMED") en la BD,
     * lo cual es mucho más legible que un número.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCita estado;

    /**
     * Motivo de cancelación de la cita.
     * Solo debe poblarse cuando el estado cambia a CANCELADA.
     */
    @Column(name = "motivo_cancelacion")
    private String motivoCancelacion;

    /**
     * Fecha y hora de creación del registro.
     * Se establece automáticamente al persistir la entidad.
     * Campo inmutable (no se actualiza en modificaciones posteriores).
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de la última modificación del registro.
     * Se actualiza automáticamente en cada cambio de la entidad.
     */
    @UpdateTimestamp
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /**
     * Obtiene el examen asociado a esta cita a través de la disponibilidad.
     * 
     * Método de conveniencia que navega por la relación CitaExamen -> Disponibilidad -> Examen
     * para facilitar el acceso en consultas GraphQL sin exponer directamente la disponibilidad.
     * 
     * @return el examen asociado, o null si la disponibilidad no está cargada
     */
    public Examen getExamen() {
        if (this.disponibilidad != null) {
            return this.disponibilidad.getExamen();
        }
        return null;
    }

    /**
     * Obtiene la sede asociada a esta cita a través de la disponibilidad.
     * 
     * Método de conveniencia que navega por la relación CitaExamen -> Disponibilidad -> Sede
     * para facilitar el acceso en consultas GraphQL sin exponer directamente la disponibilidad.
     * 
     * @return la sede asociada, o null si la disponibilidad no está cargada
     */
    public Sede getSede() {
        if (this.disponibilidad != null) {
            return this.disponibilidad.getSede();
        }
        return null;
    }

}
