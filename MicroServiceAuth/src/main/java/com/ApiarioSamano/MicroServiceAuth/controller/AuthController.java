package com.ApiarioSamano.MicroServiceAuth.controller;

import com.ApiarioSamano.MicroServiceAuth.dto.AuthResponse;
import com.ApiarioSamano.MicroServiceAuth.dto.LoginRequest;
import com.ApiarioSamano.MicroServiceAuth.dto.ResponseDTO;
import com.ApiarioSamano.MicroServiceAuth.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);

        ResponseDTO<AuthResponse> response = ResponseDTO.success(
                authResponse,
                "Login exitoso");

        return ResponseEntity.ok(response);
    }

}