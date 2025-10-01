package com.CitaSalud.core.services;

import com.CitaSalud.domain.entities.*;
import com.CitaSalud.domain.repository.CitaExamenRepository;
import com.CitaSalud.domain.repository.DisponibilidadRepository;
import com.CitaSalud.domain.repository.ExamenRepository;
import com.CitaSalud.domain.repository.SedeRepository;
import com.CitaSalud.dto.AgendamientoDTO;
import com.CitaSalud.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.CitaSalud.domain.entities.Usuario;

import java.time.LocalDate;
import java.util.List;

@Service
public class AgendamientoExamenService {
    // Inyectamos TODOS los repositorios necesarios
    private final DisponibilidadRepository disponibilidadRepository;
    private final CitaExamenRepository citaExamenRepository;
    private final UsuarioRepository usuarioRepository;
    private final SedeRepository sedeRepository;
    private final ExamenRepository examenRepository;

    public AgendamientoExamenService(DisponibilidadRepository disponibilidadRepository, CitaExamenRepository citaExamenRepository, UsuarioRepository usuarioRepository, SedeRepository sedeRepository, ExamenRepository examenRepository) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.citaExamenRepository = citaExamenRepository;
        this.usuarioRepository = usuarioRepository;
        this.sedeRepository = sedeRepository;
        this.examenRepository = examenRepository;
    }

    /**
     * Devuelve una lista de fechas que tienen cupos disponibles a partir de hoy.
     */
    public List<LocalDate> getFechasDisponibles() {
        return disponibilidadRepository.findFechasDisponibles(LocalDate.now());
    }

    /**
     * Dado una fecha, devuelve las sedes con disponibilidad.
     */
    public List<Sede> getSedesDisponibles(LocalDate fecha) {
        return disponibilidadRepository.findSedesDisponiblesPorFecha(fecha);
    }

    /**
     * Dado una fecha y una sede, devuelve los tipos de examen disponibles.
     */
    public List<TipoExamen> getTiposExamenDisponibles(LocalDate fecha, Integer sedeId) {
        return disponibilidadRepository.findTiposExamenDisponibles(fecha, sedeId);
    }

    /**
     * Dado fecha, sede y tipo de examen, devuelve los exámenes específicos disponibles.
     */
    public List<Examen> getExamenesDisponibles(LocalDate fecha, Integer sedeId, Integer tipoExamenId) {
        return disponibilidadRepository.findExamenesDisponibles(fecha, sedeId, tipoExamenId);
    }

    @Transactional
    public CitaExamen agendarExamen(AgendamientoDTO dto) {
        // 1. Validar que la disponibilidad existe y tiene cupos (y bloquearla)
        Disponibilidad disponibilidad = disponibilidadRepository
                .findAndLockDisponibilidad(dto.getSedeId(), dto.getExamenId(), dto.getFechaHora().toLocalDate())
                .orElseThrow(() -> new RuntimeException("No hay cupos disponibles o la disponibilidad no existe."));

        // 2. Incrementar los cupos ocupados y guardar el cambio
        disponibilidad.setCuposOcupados(disponibilidad.getCuposOcupados() + 1);
        disponibilidadRepository.save(disponibilidad);

        // 3. Buscar las entidades relacionadas
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Sede sede = sedeRepository.findById(dto.getSedeId()).orElseThrow(() -> new RuntimeException("Sede no encontrada"));
        Examen examen = examenRepository.findById(dto.getExamenId()).orElseThrow(() -> new RuntimeException("Examen no encontrado"));

        // 4. Crear la nueva cita
        CitaExamen nuevaCita = new CitaExamen();
        nuevaCita.setUsuario(usuario);
        nuevaCita.setSede(sede);
        nuevaCita.setExamen(examen);
        nuevaCita.setFechaHora(dto.getFechaHora());
        nuevaCita.setEstado("AGENDADA");

        // 5. Guardar la cita y devolverla
        return citaExamenRepository.save(nuevaCita);
    }

}
