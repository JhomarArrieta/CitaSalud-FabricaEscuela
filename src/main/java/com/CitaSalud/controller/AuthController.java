package com.CitaSalud.controller;

import com.CitaSalud.core.services.AuthService;
import com.CitaSalud.dto.AuthResponse;
import com.CitaSalud.dto.AuthInput; // Importamos el nuevo DTO/Input
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @MutationMapping
    public AuthResponse login(@Argument AuthInput input) { // Usamos el Input object

        // Delegamos al servicio, usando los campos del Input.
        AuthResponse response = authService.authenticateUser(
                input.getEmail(),
                input.getContrasena()
        );
        return response;
    }
}