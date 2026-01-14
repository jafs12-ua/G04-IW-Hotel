package com.villadictos.app.controller;

import com.villadictos.app.repository.ModeloReservaRepository;
import com.villadictos.app.repository.SalaRepository;
import com.villadictos.app.repository.ServicioRepository;
import com.villadictos.app.repository.TipoHabitacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class HomeController {

    @Autowired
    private TipoHabitacionRepository tipoHabitacionRepository;

    @Autowired
    private ModeloReservaRepository modeloReservaRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private SalaRepository salaRepository;

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/servicios")
    public String servicios(Model model) {
        model.addAttribute("servicios", servicioRepository.findAll());
        return "servicios";
    }

    @GetMapping("/habitaciones")
    public String habitaciones(Model model) {
        model.addAttribute("tiposHabitacion", tipoHabitacionRepository.findAll());
        model.addAttribute("modelosReserva", modeloReservaRepository.findAll());
        return "habitaciones";
    }

    @GetMapping("/salas")
    public String salas(Model model) {
        model.addAttribute("salas", salaRepository.findAll());
        return "salas";
    }
    
    @GetMapping("/buscar-disponibilidad")
    public String buscarDisponibilidad(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam("cantidadAdultos") Integer cantidadAdultos,
            Model model) {
        
        var tiposDisponibles = tipoHabitacionRepository.findAvailableByCapacityAndDateRange(
                cantidadAdultos, fechaInicio, fechaFin);
        
        model.addAttribute("tiposHabitacion", tiposDisponibles);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);
        model.addAttribute("cantidadAdultos", cantidadAdultos);
        model.addAttribute("modelosReserva", modeloReservaRepository.findAll());
        
        return "disponibilidad";
    }
}
