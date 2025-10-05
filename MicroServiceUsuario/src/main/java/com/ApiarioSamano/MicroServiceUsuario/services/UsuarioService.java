package com.ApiarioSamano.MicroServiceUsuario.services;



import org.springframework.stereotype.Service;

import com.ApiarioSamano.MicroServiceUsuario.model.Usuario;
import com.ApiarioSamano.MicroServiceUsuario.repositoty.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Crear o actualizar un usuario
    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Obtener todos los usuarios
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    // Obtener usuario por ID
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    // Obtener usuario por Email
    public Optional<Usuario> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // Eliminar usuario
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    // Validar existencia por email
    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
}
