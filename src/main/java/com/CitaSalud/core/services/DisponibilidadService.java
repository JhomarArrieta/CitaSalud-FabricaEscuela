package com.CitaSalud.core.services;

import com.CitaSalud.domain.entities.Examen;
import com.CitaSalud.domain.entities.Sede;
import com.CitaSalud.domain.entities.TipoExamen;
import com.CitaSalud.domain.repository.DisponibilidadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DisponibilidadService {

    private final DisponibilidadRepository disponibilidadRepository;

    public DisponibilidadService(DisponibilidadRepository disponibilidadRepository) {
        this.disponibilidadRepository = disponibilidadRepository;
    }

    /**
     * Devuelve una lista de fechas que tienen cupos disponibles a partir de hoy.
     * Usamos readOnly = true para optimizar consultas que no modifican datos.
     */
    @Transactional(readOnly = true)
    public List<LocalDate> getFechasDisponibles() {
        return disponibilidadRepository.findFechasDisponibles(LocalDate.now());
    }

    /**
     * Dado una fecha, devuelve las sedes con disponibilidad.
     */
    @Transactional(readOnly = true)
    public List<Sede> getSedesDisponibles(LocalDate fecha) {
        return disponibilidadRepository.findSedesDisponiblesPorFecha(fecha);
    }

    /**
     * Dado una fecha y una sede, devuelve los tipos de examen disponibles.
     * OJO: Revisa la nota sobre los IDs al final.
     */
    @Transactional(readOnly = true)
    public List<TipoExamen> getTiposExamenDisponibles(LocalDate fecha, Long sedeId) {
        return disponibilidadRepository.findTiposExamenDisponibles(fecha, sedeId);
    }

    /**
     * Dado fecha, sede y tipo de examen, devuelve los exámenes específicos disponibles.
     * OJO: Revisa la nota sobre los IDs al final.
     */
    @Transactional(readOnly = true)
    public List<Examen> getExamenesDisponibles(LocalDate fecha, Long sedeId, Long tipoExamenId) {
        return disponibilidadRepository.findExamenesDisponibles(fecha, sedeId, tipoExamenId);
    }
}