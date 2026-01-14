package com.villadictos.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.villadictos.app.dto.CrearReservaDTO;
import com.villadictos.app.dto.ReservarServicioDTO;
import com.villadictos.app.dto.ReservarMultiplesServiciosDTO;
import com.villadictos.app.dto.ServicioItemDTO;
import com.villadictos.app.model.PendingPayment;
import com.villadictos.app.model.Reserva;
import com.villadictos.app.model.ReservaServicio;
import com.villadictos.app.repository.PendingPaymentRepository;
import com.villadictos.app.service.ReservaService;
import com.villadictos.app.service.ReservaServicioService;
import com.villadictos.app.service.TpvService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tpv")
public class TpvController {

    private final TpvService tpvService;
    private final PendingPaymentRepository pendingPaymentRepository;
    private final ReservaService reservaService;
    private final ReservaServicioService reservaServicioService;
    private final ObjectMapper objectMapper;

    public TpvController(TpvService tpvService,
            PendingPaymentRepository pendingPaymentRepository,
            ReservaService reservaService,
            ReservaServicioService reservaServicioService) {
        this.tpvService = tpvService;
        this.pendingPaymentRepository = pendingPaymentRepository;
        this.reservaService = reservaService;
        this.reservaServicioService = reservaServicioService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // For LocalDate serialization
    }

    /**
     * Callback endpoint that TPV redirects to after payment attempt
     */
    @GetMapping("/callback")
    public String handleCallback(
            @RequestParam String token,
            @RequestParam String status,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Find the pending payment
        PendingPayment pendingPayment = pendingPaymentRepository.findByToken(token)
                .orElse(null);

        if (pendingPayment == null) {
            model.addAttribute("error", "Pago no encontrado");
            model.addAttribute("message",
                    "No se pudo encontrar información del pago. Por favor, contacta con recepción.");
            return "cliente/pago-fallido";
        }

        // Verify payment status with TPV API
        try {
            TpvService.PaymentStatusResponse paymentStatus = tpvService.verifyPayment(token);

            if (paymentStatus.isCompleted()) {
                // Payment successful - complete the reservation or service
                return processSuccessfulPayment(pendingPayment, model);
            } else {
                // Payment failed
                pendingPayment.setStatus(PendingPayment.PaymentStatus.FAILED);
                pendingPayment.setFailureReason(paymentStatus.getFailureReason());
                pendingPaymentRepository.save(pendingPayment);

                model.addAttribute("error", "Pago no completado");
                model.addAttribute("message", paymentStatus.getFailureReason() != null
                        ? paymentStatus.getFailureReason()
                        : "El pago no se ha podido completar. Por favor, inténtalo de nuevo.");
                model.addAttribute("paymentType", pendingPayment.getPaymentType().name());
                return "cliente/pago-fallido";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error verificando pago");
            model.addAttribute("message", "Hubo un error al verificar el estado del pago: " + e.getMessage());
            return "cliente/pago-fallido";
        }
    }

    private String processSuccessfulPayment(PendingPayment pendingPayment, Model model) {
        try {
            if (pendingPayment.getPaymentType() == PendingPayment.PaymentType.RESERVA) {
                // Deserialize and create reservation
                CrearReservaDTO dto = objectMapper.readValue(pendingPayment.getPaymentData(), CrearReservaDTO.class);
                Reserva reserva = reservaService.crearReserva(dto);

                // Store TPV token in reserva for future refunds
                reserva.setTpvToken(pendingPayment.getToken());
                reservaService.save(reserva);

                // Mark as completed
                pendingPayment.setStatus(PendingPayment.PaymentStatus.COMPLETED);
                pendingPaymentRepository.save(pendingPayment);

                model.addAttribute("success", true);
                model.addAttribute("message", "¡Pago completado con éxito!");
                model.addAttribute("reservaId", reserva.getId());
                model.addAttribute("paymentType", "RESERVA");

                if (reserva.getSala() != null) {
                    model.addAttribute("details", "Sala: " + reserva.getSala().getNombre());
                } else if (reserva.getHabitacion() != null) {
                    model.addAttribute("details", "Habitación: " + reserva.getHabitacion().getNumeroHabitacion());
                }
                model.addAttribute("fechaInicio", reserva.getFechaInicio());
                model.addAttribute("fechaFin", reserva.getFechaFin());
                model.addAttribute("precioTotal", reserva.getPrecioTotal());

            } else if (pendingPayment.getPaymentType() == PendingPayment.PaymentType.SERVICIO) {
                // Deserialize and create service reservation
                ReservarServicioDTO dto = objectMapper.readValue(pendingPayment.getPaymentData(),
                        ReservarServicioDTO.class);
                ReservaServicio reservaServicio = reservaServicioService.reservarServicio(dto);

                // Store TPV token in reservaServicio for future refunds
                reservaServicio.setTpvToken(pendingPayment.getToken());
                reservaServicioService.save(reservaServicio);

                // Mark as completed
                pendingPayment.setStatus(PendingPayment.PaymentStatus.COMPLETED);
                pendingPaymentRepository.save(pendingPayment);

                model.addAttribute("success", true);
                model.addAttribute("message", "¡Servicio agregado con éxito!");
                model.addAttribute("reservaId", reservaServicio.getReserva().getId());
                model.addAttribute("paymentType", "SERVICIO");
                model.addAttribute("details", "Servicio: " + reservaServicio.getServicio().getNombre());
                model.addAttribute("precioTotal", pendingPayment.getAmount());
            } else if (pendingPayment.getPaymentType() == PendingPayment.PaymentType.SERVICIOS) {
                // Deserialize and create multiple service reservations
                ReservarMultiplesServiciosDTO dto = objectMapper.readValue(pendingPayment.getPaymentData(),
                        ReservarMultiplesServiciosDTO.class);
                
                int serviciosCreados = 0;
                for (ServicioItemDTO servicioItem : dto.getServicios()) {
                    ReservarServicioDTO servicioDTO = new ReservarServicioDTO();
                    servicioDTO.setIdReserva(dto.getIdReserva());
                    servicioDTO.setIdServicio(servicioItem.getIdServicio());
                    servicioDTO.setCantidad(servicioItem.getCantidad());
                    servicioDTO.setFechaInicio(servicioItem.getFechaInicio());
                    servicioDTO.setFechaFin(servicioItem.getFechaFin());
                    
                    ReservaServicio reservaServicio = reservaServicioService.reservarServicio(servicioDTO);
                    
                    // Store TPV token in reservaServicio for future refunds
                    reservaServicio.setTpvToken(pendingPayment.getToken());
                    reservaServicioService.save(reservaServicio);
                    serviciosCreados++;
                }

                // Mark as completed
                pendingPayment.setStatus(PendingPayment.PaymentStatus.COMPLETED);
                pendingPaymentRepository.save(pendingPayment);

                model.addAttribute("success", true);
                model.addAttribute("message", "¡Servicios agregados con éxito!");
                model.addAttribute("reservaId", dto.getIdReserva());
                model.addAttribute("paymentType", "SERVICIOS");
                model.addAttribute("details", serviciosCreados + " servicio(s) agregado(s) a tu reserva");
                model.addAttribute("precioTotal", pendingPayment.getAmount());
            }

            return "cliente/pago-exitoso";

        } catch (Exception e) {
            model.addAttribute("error", "Error procesando reserva");
            model.addAttribute("message",
                    "El pago se completó pero hubo un error al crear la reserva: " + e.getMessage() +
                            ". Por favor, contacta con recepción con tu número de token: " + pendingPayment.getToken());
            return "cliente/pago-fallido";
        }
    }
}
