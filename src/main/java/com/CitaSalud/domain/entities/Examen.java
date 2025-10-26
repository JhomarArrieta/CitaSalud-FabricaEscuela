package com.CitaSalud.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

/**
 * Entidad JPA que representa un examen médico disponible en el sistema.
 * 
 * Mapea la tabla "examen" y almacena información sobre:
 * - Identificación y nombre del examen
 * - Descripción detallada del procedimiento
 * - Preparación requerida para el paciente
 * - Clasificación por tipo de examen
 * - Disponibilidades asociadas en diferentes sedes y horarios
 * 
 * Relaciones:
 * - ManyToOne con TipoExamen: múltiples exámenes pueden pertenecer a un mismo tipo
 * - OneToMany con Disponibilidad: un examen puede tener múltiples franjas disponibles
 * 
 * El cascade ALL y orphanRemoval en disponibilidades permite gestionar automáticamente
 * la eliminación de franjas horarias cuando se elimina un examen.
 */
@Data
@Entity
@Table(name = "examen")
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_examen")
    private Long id;

    /**
     * Nombre del examen médico.
     * Ejemplo: "Hemograma completo", "Radiografía de tórax"
     */
    @Column(name = "nombre_examen", length = 150, nullable = false)
    private String nombre;

    /**
     * Descripción detallada del examen.
     * Explica en qué consiste el procedimiento y qué evalúa.
     */
    @Column(name = "descripcion_examen", columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Instrucciones de preparación para el paciente.
     * Ejemplo: "Ayuno de 8 horas", "No consumir medicamentos 24h antes"
     */
    @Column(name = "preparacion_requerida", columnDefinition = "TEXT")
    private String preparacionRequerida;

    @ManyToOne
    @JoinColumn(name = "id_tipo_examen", nullable = false)
    private TipoExamen tipoExamen;

    /**
     * Lista de disponibilidades asociadas a este examen.
     * Representa las franjas horarias y sedes donde se puede realizar.
     * 
     * Configuración:
     * - cascade ALL: operaciones en Examen se propagan a Disponibilidad
     * - orphanRemoval: disponibilidades huérfanas se eliminan automáticamente
     */
    @OneToMany(mappedBy = "examen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Disponibilidad> disponibilidades;

}
