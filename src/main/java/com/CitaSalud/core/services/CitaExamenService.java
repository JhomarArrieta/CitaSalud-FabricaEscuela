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
public class CitaExamenService {

    // Inyectamos solo los repositorios que ESTE servicio necesita
    private final DisponibilidadRepository disponibilidadRepository;
    private final CitaExamenRepository citaExamenRepository;
    private final UsuarioRepository usuarioRepository;

    public CitaExamenService(DisponibilidadRepository disponibilidadRepository,
                             CitaExamenRepository citaExamenRepository,
                             UsuarioRepository usuarioRepository) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.citaExamenRepository = citaExamenRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public CitaExamen agendarExamen(AgendamientoDTO dto) {

        // 1. Validar que el usuario existe (es mejor validar esto ANTES de bloquear la BD)
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + dto.getUsuarioId()));
        // En producción: .orElseThrow(() -> new RecursoNoEncontradoException("Usuario..."));


        // 2. Validar que la disponibilidad existe Y bloquear la fila
        // Usamos el bloqueo pesimista que definiste en el repositorio
        Disponibilidad disponibilidad = disponibilidadRepository
                .findAndLockDisponibilidad(
                        dto.getSedeId(),
                        dto.getExamenId(),
                        dto.getFechaHora().toLocalDate(), // La fecha
                        dto.getFechaHora().toLocalTime()  // <-- AÑADE ESTO (La hora)
                )
                .orElseThrow(() -> new RuntimeException("No hay cupos disponibles o la disponibilidad no existe."));
        // En producción: .orElseThrow(() -> new CuposAgotadosException("No hay cupos..."));

        // 3. Incrementar el cupo usando la LÓGICA DE LA ENTIDAD
        // Esto es mucho más limpio y seguro.
        disponibilidad.ocuparCupo();

        // 4. Persistir el cambio en la disponibilidad
        // Aunque @Transactional hace "dirty checking", guardar explícitamente es una buena práctica.
        // El bloqueo pesimista se libera al final de la transacción.
        disponibilidadRepository.save(disponibilidad);

        // 5. Crear la nueva cita
        CitaExamen nuevaCita = new CitaExamen();
        nuevaCita.setUsuario(usuario);
        nuevaCita.setDisponibilidad(disponibilidad);
        nuevaCita.setFechaHora(dto.getFechaHora());
        nuevaCita.setEstado("AGENDADA"); // El valor por defecto, pero es bueno ser explícito


        // 6. Guardar la cita y devolverla
        return citaExamenRepository.save(nuevaCita);
    }

}
