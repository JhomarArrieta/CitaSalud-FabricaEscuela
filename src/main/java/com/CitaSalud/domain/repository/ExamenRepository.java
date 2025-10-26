package com.CitaSalud.domain.repository;

import com.CitaSalud.domain.entities.Examen;
import com.CitaSalud.domain.entities.TipoExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la gestión de exámenes médicos.
 *
 * Proporciona operaciones CRUD estándar sobre la entidad Examen, además de
 * consultas personalizadas para:
 * - Buscar exámenes por tipo/categoría
 * - Buscar exámenes por nombre (búsqueda parcial, insensible a mayúsculas)
 *
 * Spring Data JPA genera automáticamente las implementaciones de los métodos
 * derivados basándose en sus nombres (query methods).
 *
 * Este repositorio es utilizado para:
 * - Consultar exámenes disponibles en el flujo de agendamiento
 * - Filtrar y buscar exámenes por categoría
 * - Implementar funcionalidades de búsqueda para usuarios
 */
@Repository
public interface ExamenRepository extends JpaRepository<Examen, Long> {

    /**
     * Busca todos los exámenes que pertenecen a un tipo específico.
     *
     * Útil para obtener todos los exámenes de una categoría determinada
     * (por ejemplo, todos los exámenes de "Laboratorio" o "Imagenología").
     *
     * @param tipoExamen tipo de examen por el cual filtrar
     * @return lista de exámenes que pertenecen al tipo especificado
     */
    List<Examen> findByTipoExamen(TipoExamen tipoExamen);

    /**
     * Busca exámenes cuyo nombre contenga el texto especificado.
     *
     * La búsqueda es:
     * - Parcial: encuentra coincidencias en cualquier parte del nombre
     * - Case-insensitive: ignora mayúsculas y minúsculas
     *
     * Útil para implementar funcionalidades de autocompletado o búsqueda
     * por parte del usuario.
     *
     * Ejemplo: buscar "hemo" encontrará "Hemograma", "Hemoglobina", etc.
     *
     * @param nombre texto a buscar dentro del nombre del examen
     * @return lista de exámenes cuyos nombres contienen el texto especificado
     */
    List<Examen> findByNombreContainingIgnoreCase(String nombre);

}