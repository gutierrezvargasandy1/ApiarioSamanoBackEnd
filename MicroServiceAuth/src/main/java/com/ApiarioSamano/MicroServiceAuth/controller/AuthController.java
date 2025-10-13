package com.ApiarioSamano.MicroServiceAuth.controller;

import com.ApiarioSamano.MicroServiceAuth.dto.AuthResponse;
import com.ApiarioSamano.MicroServiceAuth.dto.LoginRequest;
import com.ApiarioSamano.MicroServiceAuth.dto.ResponseDTO;
import com.ApiarioSamano.MicroServiceAuth.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements IAuthController {

    private final AuthService authService;

    @Override
    public ResponseEntity<ResponseDTO<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(ResponseDTO.success(authResponse, "Login exitoso"));
    }

    @Override
    public ResponseEntity<ResponseDTO<String>> iniciarRecuperacion(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.iniciarRecuperacion(email);

        return ResponseEntity.ok(ResponseDTO.success(
                "OTP enviado correctamente",
                "Se ha enviado un código OTP al correo " + email));
    }

    @Override
    public ResponseEntity<ResponseDTO<String>> verificarOtpYCambiarContrasena(
            @RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String nuevaContrasena = request.get("nuevaContrasena");

        authService.verificarOtpYCambiarContrasena(email, otp, nuevaContrasena);

        return ResponseEntity.ok(ResponseDTO.success(
                "Contraseña actualizada correctamente",
                "La contraseña ha sido cambiada exitosamente."));
    }
}
