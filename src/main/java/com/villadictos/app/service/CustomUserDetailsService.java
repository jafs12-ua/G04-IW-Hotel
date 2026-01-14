package com.villadictos.app.service;

import com.villadictos.app.model.Usuario;
import com.villadictos.app.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        return new User(
                usuario.getEmail(),
                usuario.getPasswordHash(),
                getAuthorities(usuario));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Usuario usuario) {
        if (usuario.getRol() == null) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        }
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name().toUpperCase()));
    }
}
