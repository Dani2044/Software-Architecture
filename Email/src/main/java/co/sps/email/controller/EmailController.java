package co.sps.email.controller;

import co.sps.email.model.NotificacionEmail;
import co.sps.email.model.NotificacionRequest;
import co.sps.email.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/enviar")
    public ResponseEntity<NotificacionEmail> enviar(@Valid @RequestBody NotificacionRequest request) {
        NotificacionEmail resultado = emailService.enviar(request);
        HttpStatus status = resultado.getEstado() == NotificacionEmail.EstadoEnvio.ENVIADO
                ? HttpStatus.OK : HttpStatus.ACCEPTED;
        return ResponseEntity.status(status).body(resultado);
    }

    @GetMapping("/compra/{numeroCompra}")
    public ResponseEntity<List<NotificacionEmail>> porCompra(@PathVariable String numeroCompra) {
        return ResponseEntity.ok(emailService.consultarPorCompra(numeroCompra));
    }

    @GetMapping("/fallidos")
    public ResponseEntity<List<NotificacionEmail>> fallidos() {
        return ResponseEntity.ok(emailService.consultarFallidos());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "servicio", "Email",
                "nodo", "Core-A",
                "ip", "10.43.100.122"
        ));
    }
}
