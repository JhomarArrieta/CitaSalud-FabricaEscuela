package com.CitaSalud.core.services;

import com.CitaSalud.domain.entities.*;
import com.CitaSalud.domain.repository.CitaExamenRepository;
import com.CitaSalud.domain.repository.DisponibilidadRepository;
import com.CitaSalud.dto.AgendamientoDTO;
import com.CitaSalud.domain.repository.UsuarioRepository;
import com.CitaSalud.exceptions.CuposAgotadosException;
import com.CitaSalud.exceptions.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.CitaSalud.domain.entities.Usuario;


/**
 * Servicio encargado de la lógica de negocio para el agendamiento de citas de exámenes médicos.
 *
 * Responsabilidades:
 * - Validar la existencia del usuario que solicita la cita
 * - Verificar y bloquear la disponibilidad del examen en la sede y fecha solicitada
 * - Gestionar el control de cupos disponibles
 * - Crear y persistir la cita agendada
 *
 * La transaccionalidad garantiza la consistencia de datos en operaciones concurrentes.
 */
@Service
public class CitaExamenService {

    private final DisponibilidadRepository disponibilidadRepository;
    private final CitaExamenRepository citaExamenRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param disponibilidadRepository repositorio para gestionar disponibilidades de exámenes
     * @param citaExamenRepository repositorio para persistir citas agendadas
     * @param usuarioRepository repositorio para validar usuarios
     */
    public CitaExamenService(DisponibilidadRepository disponibilidadRepository,
                             CitaExamenRepository citaExamenRepository,
                             UsuarioRepository usuarioRepository) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.citaExamenRepository = citaExamenRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Agenda un examen médico para un usuario en una fecha, hora y sede específicas.
     * 
     * Proceso:
     * 1. Valida la existencia del usuario
     * 2. Bloquea la disponibilidad con lock pesimista para evitar race conditions
     * 3. Verifica que existan cupos disponibles
     * 4. Ocupa un cupo en la disponibilidad
     * 5. Crea y persiste la nueva cita con estado "AGENDADA"
     * 
     * @param dto objeto con los datos del agendamiento (usuarioId, sedeId, examenId, fechaHora)
     * @return la cita creada y persistida
     * @throws RecursoNoEncontradoException si el usuario no existe
     * @throws CuposAgotadosException si no hay cupos disponibles o la disponibilidad no existe
     */
    @Transactional
    public CitaExamen agendarExamen(AgendamientoDTO dto) {

        // Validar que el usuario exista en el sistema
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + dto.getUsuarioId()));

        // Buscar y bloquear la disponibilidad para evitar reservas simultáneas del mismo cupo
        // Usa bloqueo pesimista (PESSIMISTIC_WRITE) para garantizar exclusividad durante la transacción
        Disponibilidad disponibilidad = disponibilidadRepository
                .findAndLockDisponibilidad(
                        dto.getSedeId(),
                        dto.getExamenId(),
                        dto.getFechaHora().toLocalDate(),
                        dto.getFechaHora().toLocalTime()
                )
                .orElseThrow(() -> new CuposAgotadosException("No hay cupos disponibles o la disponibilidad no existe."));

        // Ocupar un cupo en la disponibilidad (incrementa cuposOcupados)
        disponibilidad.ocuparCupo();

        // Persistir el cambio en la disponibilidad
        disponibilidadRepository.save(disponibilidad);

        // Crear la nueva cita con los datos proporcionados
        CitaExamen nuevaCita = new CitaExamen();
        nuevaCita.setUsuario(usuario);
        nuevaCita.setDisponibilidad(disponibilidad);
        nuevaCita.setFechaHora(dto.getFechaHora());
        nuevaCita.setEstado("AGENDADA");

        // Persistir y retornar la cita agendada
        return citaExamenRepository.save(nuevaCita);
    }

}
