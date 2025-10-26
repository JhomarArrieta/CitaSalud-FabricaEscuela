package com.CitaSalud.core.services;

import com.CitaSalud.domain.entities.*;
import com.CitaSalud.domain.repository.CitaExamenRepository;
import com.CitaSalud.domain.repository.DisponibilidadRepository;
import com.CitaSalud.domain.repository.ExamenRepository;
import com.CitaSalud.domain.repository.SedeRepository;
import com.CitaSalud.dto.AgendamientoDTO;
import com.CitaSalud.domain.repository.UsuarioRepository;
import com.CitaSalud.exceptions.CuposAgotadosException;
import com.CitaSalud.exceptions.RecursoNoEncontradoException;
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

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + dto.getUsuarioId()));

        Disponibilidad disponibilidad = disponibilidadRepository
                .findAndLockDisponibilidad(
                        dto.getSedeId(),
                        dto.getExamenId(),
                        dto.getFechaHora().toLocalDate(),
                        dto.getFechaHora().toLocalTime()
                )
                .orElseThrow(() -> new CuposAgotadosException("No hay cupos disponibles o la disponibilidad no existe."));

        disponibilidad.ocuparCupo();

        disponibilidadRepository.save(disponibilidad);

        CitaExamen nuevaCita = new CitaExamen();
        nuevaCita.setUsuario(usuario);
        nuevaCita.setDisponibilidad(disponibilidad);
        nuevaCita.setFechaHora(dto.getFechaHora());
        nuevaCita.setEstado("AGENDADA");


        return citaExamenRepository.save(nuevaCita);
    }

}
