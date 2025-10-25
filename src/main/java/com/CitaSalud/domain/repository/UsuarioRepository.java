package com.CitaSalud.domain.repository;

import com.CitaSalud.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link Usuario}.
 *
 * Proporciona operaciones CRUD y utilidades de consulta sobre la tabla "usuario".
 * Extiende {@link JpaRepository} para aprovechar los métodos estándar (save, findById, findAll, delete, etc.).
 *
 * Buenas prácticas:
 * - Mantener aquí consultas derivadas y firmas claras para uso desde la capa de servicio.
 * - Evitar lógica de negocio compleja en el repositorio; delegar a servicios.
 * - Documentar métodos personalizados para que su intención quede explícita.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su correo electrónico.
     *
     * Uso típico: autenticación y recuperación de perfil por email.
     * Retorna {@link Optional} para obligar al llamador a manejar ausencia del usuario.
     *
     * @param email correo electrónico a buscar
     * @return Optional que contiene el {@link Usuario} si se encuentra, o vacío en caso contrario
     */
    Optional<Usuario> findByEmail(String email);
}
