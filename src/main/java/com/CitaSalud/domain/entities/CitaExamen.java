package com.CitaSalud.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "cita_examen")
public class CitaExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Integer id;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "estado", length = 20)
    private String estado; // Ej: "AGENDADA", "CANCELADA", "COMPLETADA"

    @Column(name = "motivo_cancelacion")
    private String motivoCancelacion;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private ZonedDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_modificacion")
    private ZonedDateTime fechaModificacion;

    // --- Relaciones ---
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_sede", nullable = false)
    private Sede sede;

    @ManyToOne
    @JoinColumn(name = "id_examen", nullable = false)
    private Examen examen;

}
