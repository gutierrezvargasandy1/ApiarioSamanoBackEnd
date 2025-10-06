package com.ApiarioSamano.MicroServiceAuth.services;

import com.ApiarioSamano.MicroServiceAuth.dto.AuthResponse;
import com.ApiarioSamano.MicroServiceAuth.dto.LoginRequest;
import com.ApiarioSamano.MicroServiceAuth.exception.CredencialesInvalidasException;
import com.ApiarioSamano.MicroServiceAuth.exception.UsuarioNoEncontradoException;
import com.ApiarioSamano.MicroServiceAuth.model.Usuario;
import com.ApiarioSamano.MicroServiceAuth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByemail(request.getEmail())
                .orElseThrow(() -> new UsuarioNoEncontradoException(request.getEmail()));

        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            throw new CredencialesInvalidasException();
        }

        String token = jwtService.generateToken(
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getId());

        return AuthResponse.builder()
                .token(token)
                .build();
    }

}