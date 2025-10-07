package com.ApiarioSamano.MicroServiceUsuario.repositoty;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ApiarioSamano.MicroServiceUsuario.model.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar usuario por email
    Optional<Usuario> findByEmail(String email);

    // Verificar si existe un usuario con un email
    boolean existsByEmail(String email);

    Optional<Usuario> findByRol(String rol);
}
