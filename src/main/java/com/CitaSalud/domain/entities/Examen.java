package com.CitaSalud.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "examen")
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_examen")
    private Integer id;

    @Column(name = "nombre_examen", length = 150, nullable = false)
    private String nombre;

    @Column(name = "descripcion_examen")
    private String descripcion;

    @Column(name = "preparacion_requerida")
    private String preparacionRequerida;

    // --- Relaciones ---
    @ManyToOne
    @JoinColumn(name = "id_tipo_examen", nullable = false)
    private TipoExamen tipoExamen;
}
