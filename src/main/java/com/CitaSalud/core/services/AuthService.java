package com.CitaSalud.core.services;

import com.CitaSalud.domain.entities.Usuario;
import com.CitaSalud.domain.repository.UsuarioRepository;
import com.CitaSalud.dto.AuthResponse;
import com.CitaSalud.exceptions.BadCredentialsException;
import com.CitaSalud.exceptions.UnauthorizedException;
import com.CitaSalud.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider){
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    public AuthResponse authenticateUser(String email, String contrasena){

        Usuario usuario = usuarioRepository.findByEmailWithRol(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas."));
        System.out.println("Usuario encontrado: " + usuario.getEmail() + " / pass: " + usuario.getContrasena() + " / rol: " + usuario.getIdRol().getIdRol());


        //if (!passwordEncoder.matches(contrasena, usuario.getContrasena())){
        //     throw new BadCredentialsException("Credenciales invalidas.");
        //}
        if (!contrasena.equals(usuario.getContrasena())) {
            throw new BadCredentialsException("Credenciales inválidas.");
        }

        final long rolAutorizado = 1L;

        if (usuario.getIdRol() == null || !usuario.getIdRol().getIdRol().equals(rolAutorizado)){
            throw new UnauthorizedException("Rol no autorizado para acceder al modulo de agendamiento.");
        }

        String rolNombre = usuario.getIdRol().getNombreRol();

        String token = jwtTokenProvider.generateToken(usuario.getIdUsuario(), rolNombre);

        return new AuthResponse(token);


    }

}
