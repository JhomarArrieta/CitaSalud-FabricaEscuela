package com.CitaSalud.domain.repository;

import com.CitaSalud.domain.entities.TipoExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la gestión de tipos o categorías de exámenes médicos.
 *
 * Proporciona operaciones CRUD sobre la entidad TipoExamen, además de
 * consultas personalizadas para buscar tipos por nombre.
 *
 * Spring Data JPA genera automáticamente las implementaciones de los métodos
 * estándar y derivados basándose en la interfaz JpaRepository.
 *
 * Este repositorio es utilizado para:
 * - Consultar tipos de examen disponibles en el flujo de agendamiento
 * - Buscar categorías específicas por nombre (ej: "Laboratorio", "Imagenología")
 * - Administración de categorías de exámenes del sistema
 */
@Repository
public interface TipoExamenRepository extends JpaRepository<TipoExamen, Long> {

    /**
     * Busca un tipo de examen por su nombre exacto, ignorando mayúsculas y minúsculas.
     *
     * Útil para encontrar una categoría específica cuando se conoce su nombre,
     * sin preocuparse por el formato del texto (mayúsculas/minúsculas).
     *
     * Ejemplo: buscar "laboratorio", "LABORATORIO" o "Laboratorio" retornará
     * el mismo tipo de examen.
     *
     * @param nombre nombre del tipo de examen a buscar
     * @return el tipo de examen encontrado, o null si no existe
     */
    TipoExamen findByNombreIgnoreCase(String nombre);
}