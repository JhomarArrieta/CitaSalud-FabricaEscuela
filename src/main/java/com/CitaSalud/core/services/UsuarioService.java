package com.CitaSalud.core.services;

import com.CitaSalud.domain.entities.Usuario;
import com.CitaSalud.domain.repository.UsuarioRepository;
import com.CitaSalud.dto.ActualizarPerfilInput;
import com.CitaSalud.exceptions.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Obtiene el perfil de un usuario por su ID.
     */
    @Transactional(readOnly = true)
    public Usuario getPerfil(Long usuarioId) throws RecursoNoEncontradoException {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + usuarioId));
    }

    /**
     * Actualiza el perfil de un usuario (HU-003).
     */
    @Transactional
    public Usuario actualizarPerfil(Long usuarioId, ActualizarPerfilInput input) throws RecursoNoEncontradoException {

        Usuario usuario = getPerfil(usuarioId); // Reutiliza el método anterior para encontrar al usuario

        // Mapea los campos del input a la entidad (solo si no son nulos)
        // Esto permite al frontend enviar solo los campos que quiere cambiar.
        if (input.getNombre() != null) {
            usuario.setNombre(input.getNombre());
        }
        if (input.getApellido() != null) {
            usuario.setApellido(input.getApellido());
        }
        if (input.getTipoDocumento() != null) {
            usuario.setTipoDocumento(input.getTipoDocumento());
        }
        if (input.getNumeroDocumento() != null) {
            usuario.setNumeroDocumento(input.getNumeroDocumento());
        }
        if (input.getTelefono() != null) {
            usuario.setTelefono(input.getTelefono());
        }
        if (input.getEmail() != null) {
            // Validar que el nuevo correo no esté en uso por OTRO usuario
            // Usamos el 'findByEmail' que ya tenías en tu repositorio
            Optional<Usuario> otroUsuario = usuarioRepository.findByEmail(input.getEmail());
            if (otroUsuario.isPresent() && !otroUsuario.get().getIdUsuario().equals(usuarioId)) {
                throw new IllegalStateException("El correo electrónico ya está en uso por otra cuenta.");
            }
            usuario.setEmail(input.getEmail());
        }

        // Guarda y devuelve el usuario actualizado
        return usuarioRepository.save(usuario);
    }
}
