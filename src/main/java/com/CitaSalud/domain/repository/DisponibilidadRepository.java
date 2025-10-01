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
import java.util.List;
import java.util.Optional;

@Repository
public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Integer> {

    @Query("SELECT DISTINCT d.fecha FROM Disponibilidad d WHERE d.fecha >= :fechaActual AND (d.cuposTotales - d.cuposOcupados) > 0 ORDER BY d.fecha ASC")
    List<LocalDate> findFechasDisponibles(LocalDate fechaActual);

    @Query("SELECT DISTINCT d.sede FROM Disponibilidad d WHERE d.fecha = :fecha AND (d.cuposTotales - d.cuposOcupados) > 0")
    List<Sede> findSedesDisponiblesPorFecha(LocalDate fecha);

    @Query("SELECT DISTINCT d.examen.tipoExamen FROM Disponibilidad d WHERE d.fecha = :fecha AND d.sede.id = :sedeId AND (d.cuposTotales - d.cuposOcupados) > 0")
    List<TipoExamen> findTiposExamenDisponibles(LocalDate fecha, Integer sedeId);

    @Query("SELECT DISTINCT d.examen FROM Disponibilidad d WHERE d.fecha = :fecha AND d.sede.id = :sedeId AND d.examen.tipoExamen.id = :tipoExamenId AND (d.cuposTotales - d.cuposOcupados) > 0")
    List<Examen> findExamenesDisponibles(LocalDate fecha, Integer sedeId, Integer tipoExamenId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("FROM Disponibilidad d WHERE d.sede.id = :sedeId AND d.examen.id = :examenId AND d.fecha = :fecha AND (d.cuposTotales - d.cuposOcupados) > 0")
    Optional<Disponibilidad> findAndLockDisponibilidad(Integer sedeId, Integer examenId, LocalDate fecha);
}

