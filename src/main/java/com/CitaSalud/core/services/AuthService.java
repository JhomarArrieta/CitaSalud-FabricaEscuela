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

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse authenticateUser(String email, String contrasena) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        Set<String> roles = usuario.getRoles().stream().map(r -> r.getNombreRol()).collect(Collectors.toSet());

        String token = jwtTokenProvider.generateToken(usuario.getIdUsuario(), roles);

        return new AuthResponse(token);
    }
}