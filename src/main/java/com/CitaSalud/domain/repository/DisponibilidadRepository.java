package com.CitaSalud.domain.repository;

import com.CitaSalud.domain.entities.Disponibilidad;
import com.CitaSalud.domain.entities.Examen;
import com.CitaSalud.domain.entities.Sede;
import com.CitaSalud.domain.entities.TipoExamen;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la gestión de disponibilidades de exámenes médicos.
 *
 * Proporciona consultas especializadas para el flujo de agendamiento de citas:
 * - Fechas con disponibilidad
 * - Sedes disponibles por fecha
 * - Tipos de examen por fecha y sede
 * - Exámenes específicos por múltiples filtros
 * - Bloqueo pesimista para reserva concurrente
 *
 * Este repositorio es fundamental para garantizar la integridad de los agendamientos
 * y evitar sobrerreservas mediante el uso de bloqueos a nivel de base de datos.
 */
@Repository
public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {

    /**
     * Obtiene todas las fechas futuras que tienen al menos un cupo disponible.
     *
     * Filtra disponibilidades desde la fecha actual en adelante donde:
     * - cuposTotales > cuposOcupados
     *
     * Retorna fechas únicas ordenadas ascendentemente.
     *
     * @param fechaActual fecha desde la cual buscar disponibilidad
     * @return lista de fechas con cupos disponibles
     */
    @Query("SELECT DISTINCT d.fecha FROM Disponibilidad d WHERE d.fecha >= :fechaActual AND (d.cuposTotales - d.cuposOcupados) > 0 ORDER BY d.fecha ASC")
    List<LocalDate> findFechasDisponibles(LocalDate fechaActual);

    /**
     * Obtiene las sedes que tienen disponibilidad en una fecha específica.
     *
     * Filtra sedes con al menos un examen disponible (cupos libres) en la fecha indicada.
     *
     * @param fecha fecha para consultar sedes disponibles
     * @return lista de sedes con disponibilidad
     */
    @Query("SELECT DISTINCT d.sede FROM Disponibilidad d WHERE d.fecha = :fecha AND (d.cuposTotales - d.cuposOcupados) > 0")
    List<Sede> findSedesDisponiblesPorFecha(LocalDate fecha);

    /**
     * Obtiene los tipos de examen disponibles para una fecha y sede específicas.
     *
     * Navega por la relación Disponibilidad -> Examen -> TipoExamen para obtener
     * las categorías de exámenes que tienen cupos libres.
     *
     * @param fecha fecha de la cita deseada
     * @param sedeId identificador de la sede seleccionada
     * @return lista de tipos de examen con disponibilidad
     */
    @Query("SELECT DISTINCT d.examen.tipoExamen FROM Disponibilidad d WHERE d.fecha = :fecha AND d.sede.id = :sedeId AND (d.cuposTotales - d.cuposOcupados) > 0")
    List<TipoExamen> findTiposExamenDisponibles(LocalDate fecha, Long sedeId);

    /**
     * Obtiene los exámenes específicos disponibles según fecha, sede y tipo de examen.
     *
     * Último nivel de filtrado antes de agendar. Retorna los exámenes concretos
     * que el usuario puede seleccionar.
     *
     * @param fecha fecha de la cita deseada
     * @param sedeId identificador de la sede seleccionada
     * @param tipoExamenId identificador del tipo de examen seleccionado
     * @return lista de exámenes específicos con disponibilidad
     */
    @Query("SELECT DISTINCT d.examen FROM Disponibilidad d WHERE d.fecha = :fecha AND d.sede.id = :sedeId AND d.examen.tipoExamen.id = :tipoExamenId AND (d.cuposTotales - d.cuposOcupados) > 0")
    List<Examen> findExamenesDisponibles(LocalDate fecha, Long sedeId, Long tipoExamenId);

    /**
     * Busca y bloquea una disponibilidad específica para agendamiento seguro.
     *
     * Utiliza bloqueo pesimista (PESSIMISTIC_WRITE) para garantizar exclusividad
     * durante la reserva del cupo, evitando condiciones de carrera cuando múltiples
     * usuarios intentan agendar simultáneamente la misma franja horaria.
     *
     * El bloqueo se mantiene hasta que finalice la transacción, momento en el cual
     * otros threads podrán acceder a la fila.
     *
     * Solo retorna disponibilidades con cupos libres.
     *
     * @param sedeId identificador de la sede
     * @param examenId identificador del examen
     * @param fecha fecha de la cita
     * @param horaInicio hora de inicio de la franja horaria
     * @return Optional con la disponibilidad bloqueada, o vacío si no existe o no hay cupos
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("FROM Disponibilidad d WHERE d.sede.id = :sedeId " +
            "AND d.examen.id = :examenId " +
            "AND d.fecha = :fecha " +
            "AND d.horaInicio = :horaInicio " +
            "AND (d.cuposTotales - d.cuposOcupados) > 0")
    Optional<Disponibilidad> findAndLockDisponibilidad(Long sedeId,
                                                       Long examenId,
                                                       LocalDate fecha,
                                                       LocalTime horaInicio);
}