package com.CitaSalud.controller;

import com.CitaSalud.core.services.AuthService;
import com.CitaSalud.dto.AuthResponse;
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
    public AuthResponse login(@Argument String email, @Argument String contrasena) {
        AuthResponse response = authService.authenticateUser(email, contrasena);
        return response;
    }
}
