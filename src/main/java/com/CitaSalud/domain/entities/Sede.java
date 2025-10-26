package com.CitaSalud.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "sede")
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sede")
    private Long id;

    @Column(name = "nombre_sede", length = 100, nullable = false)
    private String nombreSede;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "telefono_sede", length = 20)
    private String telefonoSede;

    @OneToMany(mappedBy = "sede", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Disponibilidad> disponibilidades;

}
