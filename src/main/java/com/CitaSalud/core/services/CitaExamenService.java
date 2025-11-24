package com.CitaSalud.core.services;

import com.CitaSalud.domain.entities.*;
import com.CitaSalud.domain.repository.CitaExamenRepository;
import com.CitaSalud.domain.repository.DisponibilidadRepository;
import com.CitaSalud.dto.AgendamientoDTO;
import com.CitaSalud.domain.repository.UsuarioRepository;
import com.CitaSalud.dto.CancelacionDTO;
import com.CitaSalud.exceptions.CuposAgotadosException;
import com.CitaSalud.exceptions.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.CitaSalud.domain.entities.Usuario;

import java.time.LocalDateTime;


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
        nuevaCita.setEstado(EstadoCita.CONFIRMADO);

        // Persistir y retornar la cita agendada
        return citaExamenRepository.save(nuevaCita);
    }

    /**
     * Cancela un examen médico previamente agendado.
     *
     * Proceso:
     * 1. Valida la existencia de la cita.
     * 2. Valida que el usuario autenticado (del DTO) sea el dueño de la cita.
     * 3. Valida que la cita esté en estado "AGENDADA" (o 'pendiente' según tu lógica).
     * 4. Bloquea la disponibilidad asociada (PESSIMISTIC_WRITE) para liberar el cupo de forma segura.
     * 5. Libera un cupo en la disponibilidad (decrementa cuposOcupados).
     * 6. Actualiza el estado de la cita a "CANCELADA" y guarda el motivo.
     *
     * @param dto objeto con los datos de cancelación (usuarioId, citaId, motivo)
     * @return la cita actualizada
     * @throws RecursoNoEncontradoException si la cita o la disponibilidad no existen
     * @throws SecurityException (o similar) si el usuario no es dueño de la cita
     * @throws IllegalStateException (o similar) si la cita no se puede cancelar
     */
    @Transactional
    public CitaExamen cancelarExamen(CancelacionDTO dto) {

        // 1. Encontrar la cita
        CitaExamen cita = citaExamenRepository.findById(dto.getCitaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada con ID: " + dto.getCitaId()));

        // 2. Validación de Seguridad (CRÍTICA)
        if (!cita.getUsuario().getIdUsuario().equals(dto.getUsuarioId())) {
            // Comparamos el dueño de la cita (cita.getUsuario().getId())
            // con el usuario del token (dto.getUsuarioId())
            throw new SecurityException("No tiene permiso para cancelar esta cita.");
        }

        // ---- MODIFICADO POR LA HISTORIA DE USUARIO ----
        // 3. Validar estado (no se puede cancelar una cita ya cancelada o completada)
        if (cita.getEstado() == EstadoCita.CANCELADO || cita.getEstado() == EstadoCita.COMPLETADO) {
            throw new IllegalStateException("La cita ya está en un estado final (Cancelada o Completada) y no puede modificarse.");
        }

        // 4. Obtener la disponibilidad de la cita para bloquearla
        Disponibilidad dispAsociada = cita.getDisponibilidad();
        if (dispAsociada == null) {
            throw new IllegalStateException("Error de datos: La cita no tiene una disponibilidad asociada.");
        }

        // 5. Bloquear la disponibilidad (reutilizando la lógica de 'agendarExamen')
        LocalDateTime fechaHoraDeLaCita = cita.getFechaHora();
        if (fechaHoraDeLaCita == null) {
            throw new IllegalStateException("Error de datos: La cita no tiene fecha/hora.");
        }

        // ¡Llamamos al NUEVO método del repositorio
        Disponibilidad disponibilidadBloqueada = disponibilidadRepository
                .findAndLockForUpdate( // <--- CAMBIO DE MÉTODO
                        dispAsociada.getSede().getId(),
                        dispAsociada.getExamen().getId(),
                        fechaHoraDeLaCita.toLocalDate(),
                        fechaHoraDeLaCita.toLocalTime() // Esto se mapea al param 'horaInicio'
                )
                .orElseThrow(() -> new RecursoNoEncontradoException("Error crítico: No se encontró la disponibilidad asociada para liberar el cupo."));

        // 6. Liberar el cupo
        // (Debes añadir el método 'liberarCupo()' en tu entidad Disponibilidad)
        disponibilidadBloqueada.liberarCupo();
        disponibilidadRepository.save(disponibilidadBloqueada);

        // 7. Actualizar la cita (coincide con tu columna 'motivo_cancelacion')
        cita.setEstado(EstadoCita.CANCELADO);
        cita.setMotivoCancelacion(dto.getMotivo());

        // 8. Persistir y retornar la cita actualizada
        return citaExamenRepository.save(cita);
    }

    /**
     * Devuelve las citas pertenecientes a un usuario por su id.
     * @param usuarioId id del usuario
     * @return lista de CitaExamen del usuario
     */
    @Transactional(readOnly = true)
    public java.util.List<CitaExamen> obtenerCitasPorUsuario(Long usuarioId) {
        return citaExamenRepository.findByUsuario_IdUsuario(usuarioId);
    }

}
