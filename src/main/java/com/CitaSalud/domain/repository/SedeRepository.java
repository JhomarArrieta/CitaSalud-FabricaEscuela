package com.CitaSalud.domain.repository;

import com.CitaSalud.domain.entities.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la gestión de sedes o centros médicos.
 *
 * Proporciona operaciones CRUD sobre la entidad Sede, incluyendo:
 * - save(): crear o actualizar una sede
 * - findById(): buscar sede por su identificador
 * - findAll(): obtener todas las sedes
 * - delete(): eliminar una sede
 * - count(): contar sedes existentes
 * - existsById(): verificar existencia de una sede
 *
 * Spring Data JPA genera automáticamente las implementaciones de estos métodos
 * basándose en la interfaz JpaRepository.
 *
 * Este repositorio es utilizado para:
 * - Consultar sedes disponibles en el flujo de agendamiento
 * - Validar existencia de sedes al crear disponibilidades
 * - Administración de centros médicos del sistema
 */
@Repository
public interface SedeRepository extends JpaRepository<Sede, Long> {
}