package com.ApiarioSamano.MicroServiceAuth.controller;

import com.ApiarioSamano.MicroServiceAuth.dto.AuthResponse;
import com.ApiarioSamano.MicroServiceAuth.dto.LoginRequest;
import com.ApiarioSamano.MicroServiceAuth.dto.ResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Interfaz que define los endpoints REST para el microservicio de
 * autenticación.
 * 
 * Rutas base: {@code http://localhost:8082/api/auth}
 */
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Operaciones relacionadas con login y validación de tokens")
public interface IAuthController {

    /**
     * Iniciar sesión con credenciales de usuario.
     *
     * Método: POST
     * URL: {@code http://localhost:8082/api/auth/login}
     * Ejemplo Body:
     * {
     * "email": "usuario@correo.com",
     * "contrasena": "123456"
     * }
     *
     * @param request DTO con las credenciales
     * @return Token JWT y datos básicos de autenticación
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión con credenciales y obtener token JWT")
    ResponseEntity<ResponseDTO<AuthResponse>> login(@Valid @RequestBody LoginRequest request);

}