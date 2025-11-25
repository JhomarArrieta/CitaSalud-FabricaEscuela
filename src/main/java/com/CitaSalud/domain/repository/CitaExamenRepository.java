package com.CitaSalud.domain.repository;

import com.CitaSalud.domain.entities.CitaExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la gestión de citas de exámenes médicos.
 *
 * Proporciona operaciones CRUD sobre la entidad CitaExamen, incluyendo:
 * - save(): crear o actualizar una cita
 * - findById(): buscar cita por su identificador
 * - findAll(): obtener todas las citas
 * - delete(): eliminar una cita
 * - count(): contar citas existentes
 * - existsById(): verificar existencia de una cita
 *
 * Spring Data JPA genera automáticamente las implementaciones de estos métodos
 * basándose en la interfaz JpaRepository.
 *
 * Este repositorio es utilizado principalmente por CitaExamenService para:
 * - Persistir nuevas citas agendadas
 * - Consultar citas existentes por usuario
 * - Actualizar el estado de las citas (cancelación, finalización)
 */
@Repository
public interface CitaExamenRepository extends JpaRepository<CitaExamen, Long> {

    // Devuelve las citas asociadas a un usuario por su id (id_usuario)
    java.util.List<CitaExamen> findByUsuario_IdUsuario(Long idUsuario);

}