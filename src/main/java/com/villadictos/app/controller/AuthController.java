package com.villadictos.app.controller;

import com.villadictos.app.dto.RegistroDTO;
import com.villadictos.app.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String mostrarRegistro(Model model) {
        model.addAttribute("registroDTO", new RegistroDTO());
        return "register";
    }

    @PostMapping("/register")
    public String procesarRegistro(@Valid @ModelAttribute("registroDTO") RegistroDTO registroDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Validar que las contraseñas coincidan
        if (!registroDTO.getPassword().equals(registroDTO.getPasswordConfirm())) {
            result.rejectValue("passwordConfirm", "error.registroDTO", "Las contraseñas no coinciden");
        }

        // Validar que el email no esté registrado
        if (usuarioService.existeEmail(registroDTO.getEmail())) {
            result.rejectValue("email", "error.registroDTO", "Ya existe una cuenta con ese email");
        }

        if (result.hasErrors()) {
            return "register";
        }

        try {
            usuarioService.registrar(
                    registroDTO.getNombre(),
                    registroDTO.getEmail(),
                    registroDTO.getPassword(),
                    registroDTO.getTelefono());
            redirectAttributes.addFlashAttribute("mensaje", "¡Registro exitoso! Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Error al registrar: " + e.getMessage());
            return "register";
        }
    }
}
