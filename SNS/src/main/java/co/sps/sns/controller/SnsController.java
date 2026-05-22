package co.sps.sns.controller;

import co.sps.sns.model.SolicitudAfiliacion;
import co.sps.sns.model.ValidacionAfiliado;
import co.sps.sns.service.SnsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sns")
public class SnsController {

    private final SnsService snsService;

    public SnsController(SnsService snsService) {
        this.snsService = snsService;
    }

    @GetMapping("/validar")
    public ResponseEntity<ValidacionAfiliado> validarAfiliado(
            @RequestParam String numeroDocumento,
            @RequestParam(defaultValue = "CC") String tipoDocumento) {

        ValidacionAfiliado resultado = snsService.validarAfiliado(numeroDocumento, tipoDocumento);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/solicitudes")
    public ResponseEntity<SolicitudAfiliacion> registrarSolicitud(
            @Valid @RequestBody SolicitudAfiliacion solicitud) {

        SolicitudAfiliacion creada = snsService.registrarSolicitud(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/solicitudes/{id}")
    public ResponseEntity<SolicitudAfiliacion> consultarSolicitud(@PathVariable Long id) {
        return snsService.consultarSolicitud(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/solicitudes")
    public ResponseEntity<List<SolicitudAfiliacion>> consultarPorEstado(
            @RequestParam(defaultValue = "PENDIENTE") SolicitudAfiliacion.EstadoSolicitud estado) {

        return ResponseEntity.ok(snsService.consultarPorEstado(estado));
    }

    @PatchMapping("/solicitudes/{id}/estado")
    public ResponseEntity<SolicitudAfiliacion> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        SolicitudAfiliacion.EstadoSolicitud nuevoEstado =
                SolicitudAfiliacion.EstadoSolicitud.valueOf(body.get("estado"));
        String observaciones = body.getOrDefault("observaciones", "");

        SolicitudAfiliacion actualizada = snsService.actualizarEstado(id, nuevoEstado, observaciones);
        return ResponseEntity.ok(actualizada);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "servicio", "SNS",
                "nodo", "Core-A",
                "ip", "10.43.100.122"
        ));
    }
}
