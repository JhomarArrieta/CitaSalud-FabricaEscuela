package com.CitaSalud.controller;

import com.CitaSalud.core.services.AgendamientoExamenService;
import com.CitaSalud.domain.entities.CitaExamen;
import com.CitaSalud.domain.entities.Examen;
import com.CitaSalud.domain.entities.Sede;
import com.CitaSalud.domain.entities.TipoExamen;
import com.CitaSalud.dto.AgendamientoDTO;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AgendamientoGraphQLController {

    private final AgendamientoExamenService agendamientoService;

    public AgendamientoGraphQLController(AgendamientoExamenService agendamientoService) {
        this.agendamientoService = agendamientoService;
    }

    // --- Mapeo de Queries ---

    @QueryMapping
    public List<LocalDate> fechasDisponibles() {
        return agendamientoService.getFechasDisponibles();
    }

    @QueryMapping
    public List<Sede> sedesDisponibles(@Argument String fecha) {
        return agendamientoService.getSedesDisponibles(LocalDate.parse(fecha));
    }

    @QueryMapping
    public List<TipoExamen> tiposExamenDisponibles(@Argument String fecha, @Argument Integer sedeId) {
        return agendamientoService.getTiposExamenDisponibles(LocalDate.parse(fecha), sedeId);
    }

    @QueryMapping
    public List<Examen> examenesDisponibles(@Argument String fecha, @Argument Integer sedeId, @Argument Integer tipoExamenId) {
        return agendamientoService.getExamenesDisponibles(LocalDate.parse(fecha), sedeId, tipoExamenId);
    }

    // --- Mapeo de Mutations ---

    @MutationMapping
    public CitaExamen agendarExamen(@Argument AgendamientoInput input) {
        // Convertimos el Input de GraphQL a nuestro DTO interno
        AgendamientoDTO dto = new AgendamientoDTO();
        dto.setUsuarioId(input.usuarioId());
        dto.setSedeId(input.sedeId());
        dto.setExamenId(input.examenId());
        dto.setFechaHora(LocalDateTime.parse(input.fechaHora()));

        return agendamientoService.agendarExamen(dto);
    }

    // Pequeño truco para que GraphQL pueda manejar el 'input' del esquema
    public record AgendamientoInput(Long usuarioId, Integer sedeId, Integer examenId, String fechaHora) {}
}
