package com.villadictos.app.service;

import com.villadictos.app.model.Usuario;
import com.villadictos.app.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrar(String nombre, String email, String password, String telefono) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setRol(Usuario.Rol.cliente);
        usuario.setTelefono(telefono);
        usuario.setFechaRegistro(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Transactional
    public Usuario guardarUsuarioAdmin(Usuario usuario, String newPassword) {
        if (usuario.getId() != null) {
            Usuario existente = usuarioRepository.findById(usuario.getId()).orElseThrow();
            usuario.setFechaRegistro(existente.getFechaRegistro());

            if (newPassword == null || newPassword.isEmpty()) {
                usuario.setPasswordHash(existente.getPasswordHash());
            } else {
                usuario.setPasswordHash(passwordEncoder.encode(newPassword));
            }
        } else {
            // Nuevo usuario
            usuario.setFechaRegistro(LocalDateTime.now());
            if (newPassword != null && !newPassword.isEmpty()) {
                usuario.setPasswordHash(passwordEncoder.encode(newPassword));
            } else {
                usuario.setPasswordHash(passwordEncoder.encode("temporal123"));
            }
        }

        return usuarioRepository.save(usuario);
    }

    public java.util.List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }
}
