package com.CitaSalud.controller;

import com.CitaSalud.core.services.DisponibilidadService;
import com.CitaSalud.domain.entities.Examen;
import com.CitaSalud.domain.entities.Sede;
import com.CitaSalud.domain.entities.TipoExamen;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    // --- Mapeo de Queries ---

    @QueryMapping
    public List<LocalDate> fechasDisponibles() {
        return disponibilidadService.getFechasDisponibles();
    }

    @QueryMapping
    public List<Sede> sedesDisponibles(@Argument String fecha) {
        // La validación y parseo ocurren en el borde (controlador)
        return disponibilidadService.getSedesDisponibles(LocalDate.parse(fecha));
    }

    @QueryMapping
    public List<TipoExamen> tiposExamenDisponibles(@Argument String fecha, @Argument Long sedeId) {
        // Nota: @Argument sedeId es Long, coincidiendo con la entidad
        return disponibilidadService.getTiposExamenDisponibles(LocalDate.parse(fecha), sedeId);
    }

    @QueryMapping
    public List<Examen> examenesDisponibles(@Argument String fecha, @Argument Long sedeId, @Argument Long tipoExamenId) {
        // Nota: Los argumentos son Long
        return disponibilidadService.getExamenesDisponibles(LocalDate.parse(fecha), sedeId, tipoExamenId);
    }
}