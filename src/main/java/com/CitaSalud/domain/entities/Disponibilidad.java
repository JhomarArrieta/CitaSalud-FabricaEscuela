package com.CitaSalud.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "disponibilidad")
public class Disponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_disponibilidad")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sede", nullable = false)
    private Sede sede;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_examen", nullable = false)
    private Examen examen;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "cupos_totales", nullable = false)
    private int cuposTotales;

    @Column(name = "cupos_ocupados", nullable = false)
    private int cuposOcupados = 0;


    public boolean tieneCuposDisponibles() {
        return cuposOcupados < cuposTotales;
    }

    public void ocuparCupo() {
        if (!tieneCuposDisponibles()) {
            throw new IllegalStateException("No hay cupos disponibles para esta franja horaria.");
        }
        this.cuposOcupados++;
    }

    public void liberarCupo() {
        if (cuposOcupados > 0) {
            this.cuposOcupados--;
        } else {
            throw new IllegalStateException("No hay cupos ocupados para liberar.");
        }
    }

    public int getCuposDisponibles() {
        return cuposTotales - cuposOcupados;
    }

}
