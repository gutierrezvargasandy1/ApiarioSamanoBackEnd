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
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MicroservicioClientService microservicioClientService;

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

    // 1️⃣ Inicia la recuperación enviando OTP
    public void iniciarRecuperacion(String email) {
        Usuario usuario = usuarioRepository.findByemail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException(email));

        // Generar OTP usando microservicio
        String otp = microservicioClientService.generarOtp();

        // Guardar OTP temporalmente en el usuario
        usuario.setOtp(otp);
        usuarioRepository.save(usuario);

        // Preparar variables para el correo
        Map<String, Object> variables = Map.of(
                "nombreUsuario", usuario.getNombre(),
                "codigoVerificacion", otp,
                "fecha", java.time.LocalDate.now().toString());

        // Enviar OTP por correo usando microservicio
        microservicioClientService.enviarCorreo(
                email,
                "Código OTP para recuperación de contraseña",
                variables);
    }

    // 2️⃣ Verificar OTP y cambiar contraseña
    public void verificarOtpYCambiarContrasena(String email, String otp, String nuevaContrasena) {
        Usuario usuario = usuarioRepository.findByemail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException(email));

        if (!otp.equals(usuario.getOtp())) {
            throw new CredencialesInvalidasException();
        }

        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuario.setOtp(null); // Limpiar OTP
        usuarioRepository.save(usuario);
    }

}