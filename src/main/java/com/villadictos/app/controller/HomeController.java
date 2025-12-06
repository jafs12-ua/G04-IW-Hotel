package com.villadictos.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/servicios")
    public String servicios(Model model) {
        return "servicios";
    }

    @GetMapping("/habitaciones")
    public String habitaciones(Model model) {
        return "habitaciones";
    }

    @GetMapping("/contacto")
    public String contacto(Model model) {
        return "contacto";
    }
}
