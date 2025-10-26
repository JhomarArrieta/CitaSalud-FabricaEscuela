package com.CitaSalud.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

/**
 * Entidad JPA que representa una sede o centro médico del sistema.
 * 
 * Mapea la tabla "sede" y almacena información sobre:
 * - Identificación y nombre de la sede
 * - Ubicación física (dirección y ciudad)
 * - Información de contacto (teléfono)
 * - Disponibilidades de exámenes ofrecidas en esta sede
 * 
 * Relaciones:
 * - OneToMany con Disponibilidad: una sede puede ofrecer múltiples franjas horarias
 *   para diferentes exámenes
 * 
 * El cascade ALL y orphanRemoval permiten gestionar automáticamente las disponibilidades
 * asociadas cuando se modifica o elimina una sede.
 */
@Data
@Entity
@Table(name = "sede")
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sede")
    private Long id;

    /**
     * Nombre identificativo de la sede.
     * Ejemplo: "Sede Norte", "Centro Médico Principal"
     */
    @Column(name = "nombre_sede", length = 100, nullable = false)
    private String nombreSede;

    /**
     * Dirección física de la sede.
     * Incluye calle, número, barrio, etc.
     */
    @Column(name = "direccion", length = 255)
    private String direccion;

    /**
     * Ciudad donde se encuentra ubicada la sede.
     */
    @Column(name = "ciudad", length = 100)
    private String ciudad;

    /**
     * Teléfono de contacto de la sede.
     * Puede incluir extensión o código de área.
     */
    @Column(name = "telefono_sede", length = 20)
    private String telefonoSede;

    /**
     * Lista de disponibilidades de exámenes ofrecidas en esta sede.
     * Representa todas las franjas horarias y tipos de exámenes disponibles.
     * 
     * Configuración:
     * - cascade ALL: operaciones en Sede se propagan a Disponibilidad
     * - orphanRemoval: disponibilidades huérfanas se eliminan automáticamente
     */
    @OneToMany(mappedBy = "sede", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Disponibilidad> disponibilidades;

}
