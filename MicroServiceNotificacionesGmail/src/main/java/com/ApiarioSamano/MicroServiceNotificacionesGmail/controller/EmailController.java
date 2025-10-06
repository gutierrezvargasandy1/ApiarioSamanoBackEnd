package com.ApiarioSamano.MicroServiceNotificacionesGmail.controller;

import com.ApiarioSamano.MicroServiceNotificacionesGmail.dto.EmailRequestDTO;
import com.ApiarioSamano.MicroServiceNotificacionesGmail.services.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController implements IEmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    @PostMapping("/enviar")
    public ResponseEntity<String> enviarCorreo(@RequestBody EmailRequestDTO request) {
        emailService.enviarCorreo(request);
        return ResponseEntity.ok("Correo enviado correctamente a " + request.getDestinatario());
    }
}
