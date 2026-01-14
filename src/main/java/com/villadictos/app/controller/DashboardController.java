package com.villadictos.app.controller;

import com.villadictos.app.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UsuarioService usuarioService;

    public DashboardController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        usuarioService.buscarPorEmail(email).ifPresent(usuario -> {
            model.addAttribute("usuario", usuario);
            model.addAttribute("nombreUsuario", usuario.getNombre());
            model.addAttribute("rol", usuario.getRol().name());
        });

        return "dashboard";
    }
}
