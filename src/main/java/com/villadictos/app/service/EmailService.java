package com.villadictos.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void enviarMensajeContacto(String nombre, String emailCliente, String telefono, String asunto, String mensaje) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo("askspace.iw@gmail.com");
            message.setSubject("Mensaje de contacto: " + asunto);
            
            String contenido = String.format(
                "Nuevo mensaje de contacto recibido:\n\n" +
                "Nombre: %s\n" +
                "Email: %s\n" +
                "Teléfono: %s\n" +
                "Asunto: %s\n\n" +
                "Mensaje:\n%s",
                nombre,
                emailCliente,
                telefono != null ? telefono : "No proporcionado",
                asunto,
                mensaje
            );
            
            message.setText(contenido);
            mailSender.send(message);
            
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el correo: " + e.getMessage(), e);
        }
    }
}
