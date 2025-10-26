package com.CitaSalud.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * Entidad JPA que representa una categoría o tipo de examen médico.
 * 
 * Mapea la tabla "tipo_examen" y almacena información sobre:
 * - Identificación y nombre del tipo
 * - Descripción de la categoría
 * - Exámenes específicos que pertenecen a este tipo
 * 
 * Ejemplos de tipos de examen:
 * - Laboratorio clínico
 * - Imagenología
 * - Cardiología
 * - Endoscopia
 * 
 * Relaciones:
 * - OneToMany con Examen: un tipo puede agrupar múltiples exámenes específicos
 * 
 * Las anotaciones @ToString.Exclude y @EqualsAndHashCode.Exclude en la lista de exámenes
 * previenen recursión infinita y problemas de rendimiento al serializar o comparar entidades.
 */
@Data
@Entity
@Table(name = "tipo_examen")
public class TipoExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_examen")
    private Long id;

    /**
     * Nombre de la categoría de examen.
     * Ejemplo: "Laboratorio", "Imagenología", "Cardiología"
     */
    @Column(name = "nombre_tipo", length = 100, nullable = false)
    private String nombre;

    /**
     * Descripción detallada del tipo de examen.
     * Explica qué tipo de estudios o pruebas incluye esta categoría.
     */
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    /**
     * Lista de exámenes específicos que pertenecen a este tipo.
     * 
     * Configuración:
     * - cascade ALL: operaciones en TipoExamen se propagan a Examen
     * - orphanRemoval false: los exámenes no se eliminan automáticamente 
     *   al desasociarlos del tipo (permite reasignación a otro tipo)
     * - @ToString.Exclude: evita recursión infinita en toString()
     * - @EqualsAndHashCode.Exclude: evita problemas de rendimiento en comparaciones
     */
    @OneToMany(mappedBy = "tipoExamen", cascade = CascadeType.ALL, orphanRemoval = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Examen> examenes;

}
