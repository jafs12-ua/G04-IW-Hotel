package com.villadictos.app.controller;

import com.villadictos.app.dto.ContactoDTO;
import com.villadictos.app.model.Usuario;
import com.villadictos.app.repository.UsuarioRepository;
import com.villadictos.app.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactoController {

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/contacto")
    public String mostrarContacto(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String email = auth.getName();
            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            
            if (usuario != null) {
                model.addAttribute("usuarioNombre", usuario.getNombre());
                model.addAttribute("usuarioEmail", usuario.getEmail());
                model.addAttribute("usuarioTelefono", usuario.getTelefono() != null ? usuario.getTelefono() : "");
            }
        }
        
        return "contacto";
    }

    @PostMapping("/contacto")
    public String enviarMensaje(@ModelAttribute ContactoDTO contactoDTO, RedirectAttributes redirectAttributes) {
        try {
            emailService.enviarMensajeContacto(
                contactoDTO.getNombre(),
                contactoDTO.getEmail(),
                contactoDTO.getTelefono(),
                contactoDTO.getAsunto(),
                contactoDTO.getMensaje()
            );
            
            redirectAttributes.addFlashAttribute("success", "Tu mensaje ha sido enviado correctamente. Te responderemos pronto.");
            return "redirect:/contacto";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hubo un error al enviar tu mensaje. Por favor, inténtalo de nuevo.");
            return "redirect:/contacto";
        }
    }
}
