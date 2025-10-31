package com.CitaSalud.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entidad JPA que representa la disponibilidad de exámenes médicos en franjas horarias.
 * 
 * Mapea la tabla "disponibilidad" y almacena información sobre:
 * - Sede donde se realizará el examen
 * - Tipo de examen disponible
 * - Fecha y rango horario de la franja
 * - Cupos totales y ocupados
 * 
 * Esta entidad es fundamental para el sistema de agendamiento, permitiendo:
 * - Consultar disponibilidad de citas
 * - Controlar concurrencia en reservas mediante bloqueos pesimistas
 * - Gestionar capacidad de atención por franja horaria
 * 
 * Relaciones:
 * - ManyToOne con Sede: múltiples disponibilidades pueden pertenecer a una sede
 * - ManyToOne con Examen: múltiples disponibilidades pueden ser del mismo examen
 */
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

    /**
     * Examen médico disponible en esta franja horaria.
     * Relación obligatoria con carga diferida.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_examen", nullable = false)
    private Examen examen;

    /**
     * Fecha en que está disponible esta franja horaria.
     */
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

    /**
     * Verifica si existen cupos disponibles en esta franja horaria.
     * 
     * @return true si hay al menos un cupo libre, false si está completamente ocupada
     */
    public boolean tieneCuposDisponibles() {
        return cuposOcupados < cuposTotales;
    }

    /**
     * Ocupa un cupo en esta franja horaria.
     * 
     * Este método debe invocarse dentro de una transacción con bloqueo pesimista
     * para evitar condiciones de carrera en agendamientos concurrentes.
     * 
     * @throws IllegalStateException si no hay cupos disponibles
     */
    public void ocuparCupo() {
        if (!tieneCuposDisponibles()) {
            throw new IllegalStateException("No hay cupos disponibles para esta franja horaria.");
        }
        this.cuposOcupados++;
    }

    /**
     * Libera un cupo previamente ocupado en esta franja horaria.
     * 
     * Útil para cancelación de citas o reversión de operaciones.
     * 
     * @throws IllegalStateException si no hay cupos ocupados para liberar
     */
    public void liberarCupo() {
        if (cuposOcupados > 0) {
            this.cuposOcupados--;
        } else {
            throw new IllegalStateException("No hay cupos ocupados para liberar.");
        }
    }

    /**
     * Calcula la cantidad de cupos libres en esta franja horaria.
     * 
     * @return número de cupos disponibles (cuposTotales - cuposOcupados)
     */
    public int getCuposDisponibles() {
        return cuposTotales - cuposOcupados;
    }

}
