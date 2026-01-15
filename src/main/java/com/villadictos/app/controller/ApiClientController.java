package com.villadictos.app.controller;

import com.villadictos.app.model.ApiClient;
import com.villadictos.app.service.ApiClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para gestionar clientes API desde el panel de administración
 */
@Controller
@RequestMapping("/admin/api-clients")
public class ApiClientController {

    private final ApiClientService apiClientService;

    public ApiClientController(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    @GetMapping
    public String listClients(Model model) {
        model.addAttribute("clients", apiClientService.findAll());
        return "admin/api-clients/lista";
    }

    @GetMapping("/nuevo")
    public String newClientForm() {
        return "admin/api-clients/nuevo";
    }

    @PostMapping("/crear")
    public String createClient(@RequestParam String nombre,
            @RequestParam String empresa,
            @RequestParam String email,
            @RequestParam(required = false) String descripcion,
            RedirectAttributes redirectAttributes) {
        try {
            ApiClient client = apiClientService.createClient(nombre, empresa, email, descripcion);
            redirectAttributes.addFlashAttribute("success",
                    "Cliente creado. API Key: " + client.getApiKey());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/api-clients";
    }

    @PostMapping("/{id}/toggle")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ApiClient client = apiClientService.toggleActive(id);
        String status = client.isActivo() ? "activado" : "desactivado";
        redirectAttributes.addFlashAttribute("success", "Cliente " + status);
        return "redirect:/admin/api-clients";
    }

    @PostMapping("/{id}/regenerar")
    public String regenerateKey(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ApiClient client = apiClientService.regenerateApiKey(id);
        redirectAttributes.addFlashAttribute("success",
                "Nueva API Key generada: " + client.getApiKey());
        return "redirect:/admin/api-clients";
    }

    @PostMapping("/{id}/eliminar")
    public String deleteClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        apiClientService.deleteClient(id);
        redirectAttributes.addFlashAttribute("success", "Cliente eliminado");
        return "redirect:/admin/api-clients";
    }
}
