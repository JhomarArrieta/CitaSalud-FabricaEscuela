package com.CitaSalud.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "examen")
public class Examen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_examen")
    private Long id;

    @Column(name = "nombre_examen", length = 150, nullable = false)
    private String nombre;

    @Column(name = "descripcion_examen", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "preparacion_requerida", columnDefinition = "TEXT")
    private String preparacionRequerida;

    @ManyToOne
    @JoinColumn(name = "id_tipo_examen", nullable = false)
    private TipoExamen tipoExamen;

    @OneToMany(mappedBy = "examen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Disponibilidad> disponibilidades;

}
