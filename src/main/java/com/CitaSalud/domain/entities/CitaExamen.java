package com.CitaSalud.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una cita de examen agendada por un usuario.
 *
 * Mappea la tabla "cita_examen" y contiene información sobre:
 * - referencia al usuario que agenda la cita,
 * - la disponibilidad (franja/slot) reservada,
 * - fecha y hora de la cita,
 * - estado de la cita y motivo de cancelación (si aplica),
 * - marcas de auditoría (fecha de creación/modificación).
 *
 * Observaciones:
 * - Se usa FetchType.LAZY en relaciones ManyToOne para evitar cargar entidades relacionadas
 *   innecesariamente; acceder a los datos relacionados dentro de una transacción o usar DTOs.
 * - Lombok (@Data) proporciona getters/setters, equals/hashCode y toString;
 */
@Data
@Entity
@Table(name = "cita_examen")
public class CitaExamen {

    /**
     * Identificador primario de la cita.
     * Generado automáticamente por la base de datos (IDENTITY).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long idCita;

    /**
     * Usuario que solicita/agenda la cita.
     * - Relación ManyToOne obligatoria (optional = false).
     * - FetchType.LAZY: cargar usuario sólo cuando sea necesario.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /**
     * Disponibilidad (slot) reservada para la cita.
     * - Representa la franja horaria / recurso asociado.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "disponibilidad", nullable = false)
    private Disponibilidad disponibilidad;

    /**
     * Fecha y hora de la cita.
     * - Campo obligatorio que indica el momento efectivo de la cita.
     */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /**
     * Estado de la cita.
     * - Valor por defecto: "AGENDADA".
     * - Estados posibles (AGENDADA, CANCELADA, FINALIZADA).
     */
    @Column(name = "estado", length = 20, nullable = false)
    private String estado = "AGENDADA";

    /**
     * Motivo de cancelación.
     * - Campo opcional; se debe poblar cuando el estado cambie a "CANCELADA".
     * - No debe contener información sensible.
     */
    @Column(name = "motivo_cancelacion")
    private String motivoCancelacion;

    /**
     * Marca temporal de creación (audit).
     * - Se rellena automáticamente al persistir la entidad.
     * - updatable = false para evitar modificaciones posteriores.
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Marca temporal de última modificación (audit).
     * - Se actualiza automáticamente en cambios sobre la entidad.
     */
    @UpdateTimestamp
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;


    public Examen getExamen() {
        // Si la disponibilidad no es nula, navega a través de ella para obtener el examen
        if (this.disponibilidad != null) {
            return this.disponibilidad.getExamen();
        }
        return null;
    }

    public Sede getSede() {
        // Si la disponibilidad no es nula, navega a través de ella para obtener la sede
        if (this.disponibilidad != null) {
            return this.disponibilidad.getSede();
        }
        return null;
    }

}
