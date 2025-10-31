package com.CitaSalud.core.services;

import com.CitaSalud.domain.entities.Examen;
import com.CitaSalud.domain.entities.Sede;
import com.CitaSalud.domain.entities.TipoExamen;
import com.CitaSalud.domain.repository.DisponibilidadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio encargado de consultar la disponibilidad de exámenes médicos.
 * 
 * Proporciona métodos para obtener información sobre:
 * - Fechas con disponibilidad de citas
 * - Sedes disponibles en una fecha específica
 * - Tipos de exámenes disponibles por fecha y sede
 * - Exámenes específicos disponibles según filtros
 * 
 * Todas las operaciones son de solo lectura y optimizadas con transacciones read-only.
 * Este servicio es utilizado principalmente por el flujo de agendamiento para mostrar
 * opciones disponibles al usuario antes de confirmar una cita.
 */
@Service
public class DisponibilidadService {

    private final DisponibilidadRepository disponibilidadRepository;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param disponibilidadRepository repositorio para consultar disponibilidades
     */
    public DisponibilidadService(DisponibilidadRepository disponibilidadRepository) {
        this.disponibilidadRepository = disponibilidadRepository;
    }

    /**
     * Obtiene todas las fechas futuras que tienen al menos un cupo disponible.
     * 
     * La consulta filtra desde la fecha actual en adelante y solo incluye
     * disponibilidades donde existan cupos libres (cuposTotales > cuposOcupados).
     * 
     * @return lista de fechas disponibles ordenadas ascendentemente
     */
    @Transactional(readOnly = true)
    public List<LocalDate> getFechasDisponibles() {
        return disponibilidadRepository.findFechasDisponibles(LocalDate.now());
    }

    /**
     * Obtiene las sedes que tienen disponibilidad en una fecha específica.
     * 
     * Filtra las sedes que tienen al menos un examen con cupos disponibles
     * en la fecha proporcionada.
     * 
     * @param fecha fecha para consultar sedes disponibles
     * @return lista de sedes con disponibilidad en la fecha indicada
     */
    @Transactional(readOnly = true)
    public List<Sede> getSedesDisponibles(LocalDate fecha) {
        return disponibilidadRepository.findSedesDisponiblesPorFecha(fecha);
    }

    /**
     * Obtiene los tipos de exámenes disponibles para una fecha y sede específicas.
     * 
     * Permite al usuario conocer las categorías de exámenes que puede agendar
     * en una combinación fecha-sede determinada.
     * 
     * @param fecha fecha de la cita deseada
     * @param sedeId identificador de la sede seleccionada
     * @return lista de tipos de examen con disponibilidad
     */
    @Transactional(readOnly = true)
    public List<TipoExamen> getTiposExamenDisponibles(LocalDate fecha, Long sedeId) {
        return disponibilidadRepository.findTiposExamenDisponibles(fecha, sedeId);
    }

    /**
     * Obtiene los exámenes específicos disponibles según fecha, sede y tipo de examen.
     * 
     * Último nivel de filtrado antes de agendar. Retorna los exámenes concretos
     * que el usuario puede seleccionar para su cita.
     * 
     * @param fecha fecha de la cita deseada
     * @param sedeId identificador de la sede seleccionada
     * @param tipoExamenId identificador del tipo de examen seleccionado
     * @return lista de exámenes específicos con disponibilidad
     */
    @Transactional(readOnly = true)
    public List<Examen> getExamenesDisponibles(LocalDate fecha, Long sedeId, Long tipoExamenId) {
        return disponibilidadRepository.findExamenesDisponibles(fecha, sedeId, tipoExamenId);
    }
}