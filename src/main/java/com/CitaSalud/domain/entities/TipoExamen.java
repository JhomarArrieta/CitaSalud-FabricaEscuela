package com.CitaSalud.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "tipo_examen")
public class TipoExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_examen")
    private Integer id;

    @Column(name = "nombre_tipo", length = 100, nullable = false)
    private String nombre;

    private String descripcion;

    //Relación inversa
    @OneToMany(mappedBy = "tipoExamen")
    private List<Examen> examenes;

}
