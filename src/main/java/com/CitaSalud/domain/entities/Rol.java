package com.CitaSalud.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad JPA que representa un rol de usuario en la base de datos.
 *
 * Mapea la tabla "rol" y contiene información mínima sobre el rol:
 * - idRol: clave primaria autogenerada.
 * - nombreRol: nombre identificador del rol (ej. "ROLE_ADMIN").
 * - descripcion: texto descriptivo opcional.
 *
 * Observaciones:
 * - Lombok (@Getter, @Setter) se usa para evitar boilerplate de getters/setters.
 */
@Entity
@Table(name = "rol")
@Getter
@Setter
public class Rol {

    /**
     * Clave primaria de la entidad.
     *
     * Generada por la base de datos usando la estrategia IDENTITY.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    /**
     * Nombre técnico del rol.
     *
     * Debe usarse para control de autorización (p. ej. "ROLE_USER", "ROLE_ADMIN").
     */
    @Column(name = "nombre_rol")
    private String nombreRol;

    /**
     * Descripción human-readable del rol.
     *
     * Campo opcional para documentación interna o interfaces administrativas.
     */
    @Column(name = "descripcion")
    private String descripcion;

}
