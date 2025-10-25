package com.CitaSalud.security;

import com.CitaSalud.domain.entities.Usuario;
import com.CitaSalud.domain.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Servicio de carga de detalles de usuario para Spring Security.
 *
 * Implementa {@link UserDetailsService} para suministrar una implementación de
 * {@link UserDetails} a partir de la entidad {@link Usuario} almacenada en la base de datos.
 *
 * Responsabilidades principales:
 * - Recuperar el usuario por correo electrónico mediante {@link UsuarioRepository}.
 * - Mapear los roles de dominio (Rol) a GrantedAuthorities utilizadas por Spring Security.
 * - Lanzar {@link UsernameNotFoundException} cuando no existe el usuario solicitado.
 *
 * Observaciones de diseño:
 * - La contraseña retornada debe estar en formato hash (BCrypt u otro) tal como se almacena en la entidad.
 * - Se utiliza la implementación estándar {@link org.springframework.security.core.userdetails.User}
 *   para facilitar la integración con los mecanismos de autenticación de Spring.
 * - Si la carga de roles fuera costosa o su cardinalidad alta, considerar estrategias LAZY o caché.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Repositorio para buscar usuarios por email.
     *
     * Inyectado por constructor; no exponerlo públicamente para preservar encapsulación.
     */
    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param usuarioRepository repositorio usado para recuperar la entidad {@link Usuario}
     */
    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Carga los detalles del usuario necesarios para la autenticación.
     *
     * Flujo:
     * 1. Busca el usuario por email; si no existe lanza {@link UsernameNotFoundException}.
     * 2. Construye un {@link UserDetails} usando el email, la contraseña (hash) y las autoridades.
     * 3. Convierte cada rol de dominio en una {@link SimpleGrantedAuthority} usando el nombre del rol.
     *
     * Notas de seguridad:
     * - No exponer información sensible en el mensaje de excepción en entornos productivos.
     * - La comparación/validación de contraseña la realiza el proveedor de autenticación de Spring,
     *   por lo que aquí solo se devuelve el hash almacenado.
     *
     * @param email identificador usado como nombre de usuario (username)
     * @return UserDetails con email, contraseña (hash) y lista de autoridades (roles)
     * @throws UsernameNotFoundException si no se encuentra ningún usuario con el email dado
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        return new User(
                usuario.getEmail(),
                usuario.getContrasena(),
                usuario.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getNombreRol()))
                        .collect(Collectors.toList())
        );
    }
}
