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

/**
 * Controlador GraphQL responsable de exponer consultas relacionadas con
 * la disponibilidad de sedes, tipos de examen y exámenes.
 *
 * Responsabilidades principales:
 * - Adaptar y validar argumentos de entrada provenientes de GraphQL.
 * - Delegar la lógica de negocio a {@link DisponibilidadService}.
 * - Devolver objetos de dominio (entidades) o colecciones apropiadas para la capa de presentación.
 *
 * Notas:
 * - El controlador realiza validaciones ligeras (p. ej. parseo de fecha). Las validaciones de negocio
 *   más complejas deben residir en el servicio.
 * - Se asume que las fechas vienen en formato ISO (yyyy-MM-dd). Manejar parse exceptions en cliente o
 *   capturarlas/transformarlas en un handler global si se requiere un mensaje de error consistente.
 */
@Controller
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    /**
     * Devuelve la lista de fechas (LocalDate) con disponibilidad para agendamiento.
     *
     * @return lista de fechas disponibles; no debería retornar null (puede ser lista vacía)
     */
    @QueryMapping
    public List<LocalDate> fechasDisponibles() {
        return disponibilidadService.getFechasDisponibles();
    }

    /**
     * Devuelve las sedes que tienen disponibilidad en la fecha indicada.
     *
     * El parámetro {@code fecha} se recibe como String desde GraphQL y se parsea a {@link LocalDate}
     * usando el formato ISO. Si el formato es inválido se propagará una excepción de parseo.
     *
     * @param fecha fecha en formato ISO (yyyy-MM-dd) recibida desde la consulta GraphQL
     * @return lista de {@link Sede} disponibles en la fecha proporcionada
     * @throws java.time.format.DateTimeParseException si el String {@code fecha} no corresponde a ISO-8601
     */
    @QueryMapping
    public List<Sede> sedesDisponibles(@Argument String fecha) {
        return disponibilidadService.getSedesDisponibles(LocalDate.parse(fecha));
    }

    /**
     * Devuelve los tipos de examen disponibles para una sede en una fecha concreta.
     *
     * @param fecha  fecha en formato ISO (yyyy-MM-dd)
     * @param sedeId identificador de la sede (Long)
     * @return lista de {@link TipoExamen} disponibles
     * @throws java.time.format.DateTimeParseException si {@code fecha} no es parseable
     */
    @QueryMapping
    public List<TipoExamen> tiposExamenDisponibles(@Argument String fecha, @Argument Long sedeId) {
        return disponibilidadService.getTiposExamenDisponibles(LocalDate.parse(fecha), sedeId);
    }

    /**
     * Devuelve los exámenes disponibles para un tipo de examen en una sede y fecha determinados.
     *
     * @param fecha         fecha en formato ISO (yyyy-MM-dd)
     * @param sedeId        identificador de la sede (Long)
     * @param tipoExamenId  identificador del tipo de examen (Long)
     * @return lista de {@link Examen} disponibles
     * @throws java.time.format.DateTimeParseException si {@code fecha} no es parseable
     */
    @QueryMapping
    public List<Examen> examenesDisponibles(@Argument String fecha, @Argument Long sedeId, @Argument Long tipoExamenId) {
        return disponibilidadService.getExamenesDisponibles(LocalDate.parse(fecha), sedeId, tipoExamenId);
    }
}