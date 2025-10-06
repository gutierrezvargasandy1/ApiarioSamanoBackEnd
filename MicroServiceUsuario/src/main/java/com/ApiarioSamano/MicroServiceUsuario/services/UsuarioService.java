package com.ApiarioSamano.MicroServiceUsuario.services;

import com.ApiarioSamano.MicroServiceUsuario.exception.UsuarioAlreadyExistsException;
import com.ApiarioSamano.MicroServiceUsuario.exception.UsuarioNotFoundException;
import com.ApiarioSamano.MicroServiceUsuario.model.Usuario;
import com.ApiarioSamano.MicroServiceUsuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Crear usuario validando duplicados y encriptando contraseña
    public Usuario guardarUsuario(Usuario usuario) {
        if (usuario.getEmail() != null && usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new UsuarioAlreadyExistsException(usuario.getEmail());
        }

        // Encriptar la contraseña antes de guardar
        String contrasenaEncriptada = passwordEncoder.encode(usuario.getContrasena());
        usuario.setContrasena(contrasenaEncriptada);

        return usuarioRepository.save(usuario);
    }

    // Obtener todos los usuarios
    public List<Usuario> obtenerUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        if (usuarios.isEmpty()) {
            throw new UsuarioNotFoundException("No hay usuarios registrados en el sistema.");
        }
        return usuarios;
    }

    // Obtener usuario por ID
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
    }

    // Obtener usuario por Email
    public Usuario obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNotFoundException(email));
    }

    // Eliminar usuario
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNotFoundException(id);
        }
        usuarioRepository.deleteById(id);
    }

    // Validar existencia por email
    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    // Actualizar usuario por email
    public Usuario actualizarPorEmail(String email, Usuario usuarioActualizado) {
        Usuario usuarioExistente = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNotFoundException(email));

        // Actualizamos solo los campos que vienen no nulos
        if (usuarioActualizado.getNombre() != null) {
            usuarioExistente.setNombre(usuarioActualizado.getNombre());
        }
        if (usuarioActualizado.getApellidoPa() != null) {
            usuarioExistente.setApellidoPa(usuarioActualizado.getApellidoPa());
        }
        if (usuarioActualizado.getApellidoMa() != null) {
            usuarioExistente.setApellidoMa(usuarioActualizado.getApellidoMa());
        }
        if (usuarioActualizado.getContrasena() != null) {
            usuarioExistente.setContrasena(usuarioActualizado.getContrasena());
        }
        if (usuarioActualizado.getRol() != null) {
            usuarioExistente.setRol(usuarioActualizado.getRol());
        }
        if (usuarioActualizado.getEmail() != null && !usuarioActualizado.getEmail().equals(email)) {
            // Validar que el nuevo email no exista
            if (usuarioRepository.existsByEmail(usuarioActualizado.getEmail())) {
                throw new UsuarioAlreadyExistsException(usuarioActualizado.getEmail());
            }
            usuarioExistente.setEmail(usuarioActualizado.getEmail());
        }

        return usuarioRepository.save(usuarioExistente);
    }

}
