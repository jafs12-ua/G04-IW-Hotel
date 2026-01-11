package com.villadictos.app.controller;

import com.villadictos.app.repository.ModeloReservaRepository;
import com.villadictos.app.repository.TipoHabitacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private TipoHabitacionRepository tipoHabitacionRepository;

    @Autowired
    private ModeloReservaRepository modeloReservaRepository;

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
        model.addAttribute("tiposHabitacion", tipoHabitacionRepository.findAll());
        model.addAttribute("modelosReserva", modeloReservaRepository.findAll());
        return "habitaciones";
    }
}
