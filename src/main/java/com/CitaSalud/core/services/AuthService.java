package com.CitaSalud.core.services;

import com.CitaSalud.domain.entities.Usuario;
import com.CitaSalud.domain.repository.UsuarioRepository;
import com.CitaSalud.dto.AuthResponse;
import com.CitaSalud.exceptions.BadCredentialsException;
import com.CitaSalud.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio responsable de la autenticación de usuarios.
 *
 * Funciones principales:
 * - Validar credenciales (email + contraseña) contra el repositorio de usuarios.
 * - Verificar la contraseña usando PasswordEncoder.
 * - Generar un token JWT incluyendo los roles del usuario.
 *
 * Observaciones:
 * - Lanza BadCredentialsException cuando las credenciales son inválidas.
 * - Depende de UsuarioRepository para recuperación de usuario, PasswordEncoder para verificación
 *   segura de contraseñas y JwtTokenProvider para creación de tokens.
 */
@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param usuarioRepository repositorio para obtener la entidad Usuario por email
     * @param passwordEncoder   encoder utilizado para verificar el hash de la contraseña
     * @param jwtTokenProvider  proveedor encargado de generar y firmar tokens JWT
     */
    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Autentica a un usuario dado su email y contraseña.
     *
     * Flujo:
     * 1. Recupera el usuario por email; si no existe lanza BadCredentialsException.
     * 2. Verifica la contraseña utilizando PasswordEncoder.matches.
     * 3. Extrae los roles del usuario y genera un token JWT con la información necesaria.
     * 4. Devuelve un AuthResponse que contiene el token (y podrá extenderse con más datos si es necesario).
     *
     * @param email     correo electrónico del usuario que intenta autenticarse
     * @param contrasena contraseña en texto plano enviada por el cliente (debe transportarse sobre TLS)
     * @return AuthResponse que contiene el token JWT generado para el usuario autenticado
     * @throws BadCredentialsException si el email no existe o la contraseña no coincide
     */
    public AuthResponse authenticateUser(String email, String contrasena) {
        // Buscar usuario por correo; si no existe, no revelar detalles y devolver credenciales inválidas.
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        // Verificar la contraseña contra el hash almacenado.
        if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        // Extraer nombres de roles como un conjunto de strings para incluirlos en el token JWT.
        Set<String> roles = usuario.getRoles().stream()
                .map(r -> r.getNombreRol())
                .collect(Collectors.toSet());

        // Generar token JWT que contendrá el id del usuario y sus roles.
        String token = jwtTokenProvider.generateToken(usuario.getIdUsuario(), roles);

        // Retornar la respuesta de autenticación (actualmente sólo el token).
        return new AuthResponse(token);
    }
}