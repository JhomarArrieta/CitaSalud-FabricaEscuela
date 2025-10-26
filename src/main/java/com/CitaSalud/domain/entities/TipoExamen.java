package com.CitaSalud.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@Entity
@Table(name = "tipo_examen")
public class TipoExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_examen")
    private Long id;

    @Column(name = "nombre_tipo", length = 100, nullable = false)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @OneToMany(mappedBy = "tipoExamen", cascade = CascadeType.ALL, orphanRemoval = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Examen> examenes;

}
