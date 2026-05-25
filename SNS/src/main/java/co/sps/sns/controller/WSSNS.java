package co.sps.sns.controller;

import co.sps.sns.dto.ValidacionAfiliado;
import co.sps.sns.entity.SolicitudAfiliacion;
import co.sps.sns.service.SrvSNS;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST del servicio SNS (Superintendencia Nacional de Salud).
 *
 * <p>Expone los endpoints HTTP que permiten validar la afiliacion de un usuario
 * al sistema de salud, registrar nuevas solicitudes de afiliacion, consultar su
 * estado y actualizar el resultado de la validacion.</p>
 *
 * <p><b>Rol en la arquitectura:</b> Este controlador es el punto de entrada
 * para las llamadas asincronas realizadas por MS-Compra a traves de WebClient.
 * MS-Compra invoca principalmente el endpoint {@code GET /api/sns/validar}
 * para verificar si un plan de salud puede ser vendido.</p>
 *
 * <p><b>Base path:</b> {@code /api/sns}</p>
 *
 * @author SPS Team
 */
@RestController
@RequestMapping("/api/sns")
public class WSSNS {

    private final SrvSNS snsService;

    public WSSNS(SrvSNS snsService) {
        this.snsService = snsService;
    }

    /**
     * Endpoint principal de validacion. Acepta dos modos:
     * <ul>
     *   <li>Por codigo de plan + aseguradora (flujo principal desde MS-Compra)</li>
     *   <li>Por documento del afiliado (modo legacy/diagnostico)</li>
     * </ul>
     *
     * @return JSON con campo {@code estado}: APROBADO, RECHAZADO o ENPROCESO
     */
    @GetMapping("/validar")
    public ResponseEntity<Map<String, Object>> validar(
            @RequestParam(required = false) String codigoPlan,
            @RequestParam(required = false) String codigoAseguradora,
            @RequestParam(required = false) String numeroDocumento,
            @RequestParam(defaultValue = "CC") String tipoDocumento) {

        // Modo 1: validacion por plan (flujo principal del enunciado)
        if (codigoPlan != null && !codigoPlan.isBlank()) {
            String estado = snsService.validarPlan(codigoPlan, codigoAseguradora);
            return ResponseEntity.ok(Map.of(
                    "codigoPlan", codigoPlan,
                    "codigoAseguradora", codigoAseguradora != null ? codigoAseguradora : "",
                    "estado", estado
            ));
        }

        // Modo 2: validacion legacy por documento
        if (numeroDocumento != null && !numeroDocumento.isBlank()) {
            ValidacionAfiliado v = snsService.validarAfiliado(numeroDocumento, tipoDocumento);
            return ResponseEntity.ok(Map.of(
                    "estado", v.isAfiliado() ? "APROBADO" : "RECHAZADO",
                    "numeroDocumento", numeroDocumento,
                    "afiliado", v.isAfiliado()
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "error", "Debe enviar codigoPlan o numeroDocumento"
        ));
    }

    /**
     * Registra una nueva solicitud de afiliacion al sistema de salud.
     */
    @PostMapping("/solicitudes")
    public ResponseEntity<SolicitudAfiliacion> registrarSolicitud(
            @Valid @RequestBody SolicitudAfiliacion solicitud) {
        SolicitudAfiliacion creada = snsService.registrarSolicitud(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * Consulta una solicitud de afiliacion especifica por su identificador.
     */
    @GetMapping("/solicitudes/{id}")
    public ResponseEntity<SolicitudAfiliacion> consultarSolicitud(@PathVariable Long id) {
        return snsService.consultarSolicitud(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Consulta todas las solicitudes de afiliacion filtradas por estado.
     */
    @GetMapping("/solicitudes")
    public ResponseEntity<List<SolicitudAfiliacion>> consultarPorEstado(
            @RequestParam(defaultValue = "PENDIENTE") SolicitudAfiliacion.EstadoSolicitud estado) {
        return ResponseEntity.ok(snsService.consultarPorEstado(estado));
    }

    /**
     * Actualiza el estado de una solicitud de afiliacion existente.
     */
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

    /**
     * Endpoint de verificacion de salud (health check) del servicio SNS.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "servicio", "SNS",
                "nodo", "Core-A",
                "ip", "10.43.101.18"
        ));
    }
}
