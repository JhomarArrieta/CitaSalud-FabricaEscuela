package com.CitaSalud.domain.repository;

import com.CitaSalud.domain.entities.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para la entidad Rol.
 *
 * Proporciona operaciones CRUD y paginación/ordenamiento sobre la tabla de roles.
 * Al extender JpaRepository<Rol, Long> se disponen de métodos comunes:
 * - save, findById, findAll, delete, count, existsById, etc.
 *
 * Buenas prácticas y observaciones:
 * - Definir aquí consultas derivadas o métodos personalizados si se requieren búsquedas
 *   por atributos del rol (p. ej. findByNombreRol).
 * - No es necesario anotar con @Repository: Spring Data lo detecta automáticamente.
 * - Mantener las firmas de métodos específicas y documentarlas para evitar ambigüedad en la capa de servicio.
 */
public interface RolRepository extends JpaRepository<Rol, Long> {
    // Spring proporciona: save(), findById(), findAll(), delete(), etc.
}
