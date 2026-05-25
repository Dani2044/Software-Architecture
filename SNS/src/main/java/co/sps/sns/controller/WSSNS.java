package co.sps.sns.controller;

import co.sps.sns.model.SolicitudAfiliacion;
import co.sps.sns.model.ValidacionAfiliado;
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
 * para verificar si un usuario tiene un plan de salud activo antes de procesar
 * una compra. Las respuestas posibles son APROBADO, RECHAZADO o ENPROCESO.</p>
 *
 * <p><b>Base path:</b> {@code /api/sns}</p>
 *
 * @author SPS Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/sns")
public class WSSNS {

    private final SrvSNS snsService;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param snsService servicio de logica de negocio del SNS
     */
    public WSSNS(SrvSNS snsService) {
        this.snsService = snsService;
    }

    /**
     * Valida si un afiliado tiene un plan de salud activo en el sistema.
     *
     * <p>Este es el endpoint principal consumido por MS-Compra de forma asincrona
     * via WebClient. Busca al afiliado por numero y tipo de documento, y retorna
     * su estado de afiliacion junto con los datos de su EPS y regimen.</p>
     *
     * @param numeroDocumento numero de documento del afiliado a validar
     * @param tipoDocumento   tipo de documento (CC, TI, CE). Por defecto "CC"
     * @return respuesta HTTP 200 con el resultado de la validacion
     *         ({@link ValidacionAfiliado})
     */
    /**
     * Endpoint principal segun enunciado SPS: valida si un plan esta autorizado
     * a ser vendido por la aseguradora consultada.
     *
     * Acepta dos modos de invocacion (multiplexados para compatibilidad):
     *  - Por codigo de plan + codigo de aseguradora (uso real desde MS-Compra)
     *  - Por documento del afiliado (uso legacy/diagnostico)
     *
     * Responde con {@code {"estado": "APROBADO|RECHAZADO|ENPROCESO", ...}}
     * tal como lo consume {@link WebClientSNS} en MS-Compra.
     */
    @GetMapping("/validar")
    public ResponseEntity<Map<String, Object>> validar(
            @RequestParam(required = false) String codigoPlan,
            @RequestParam(required = false) String codigoAseguradora,
            @RequestParam(required = false) String numeroDocumento,
            @RequestParam(defaultValue = "CC") String tipoDocumento) {

        // Modo 1: validacion por plan (el flujo principal del enunciado)
        if (codigoPlan != null && !codigoPlan.isBlank()) {
            String estado = snsService.validarPlan(codigoPlan, codigoAseguradora);
            return ResponseEntity.ok(Map.of(
                    "codigoPlan", codigoPlan,
                    "codigoAseguradora", codigoAseguradora != null ? codigoAseguradora : "",
                    "estado", estado
            ));
        }

        // Modo 2: validacion legacy por documento (mantener compatibilidad)
        if (numeroDocumento != null && !numeroDocumento.isBlank()) {
            ValidacionAfiliado v = snsService.validarAfiliado(numeroDocumento, tipoDocumento);
            return ResponseEntity.ok(Map.of(
                    "estado", v.isAfiliado() ? "APROBADO" : "RECHAZADO",
                    "numeroDocumento", numeroDocumento,
                    "afiliado", v.isAfiliado()
            ));
        }

        // Sin parametros: 400
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Debe enviar codigoPlan o numeroDocumento"
        ));
    }

    /**
     * Registra una nueva solicitud de afiliacion al sistema de salud.
     *
     * <p>Recibe los datos del afiliado en el cuerpo de la peticion, los valida
     * mediante Bean Validation y crea un nuevo registro con estado PENDIENTE.</p>
     *
     * @param solicitud datos de la solicitud de afiliacion (validados con {@code @Valid})
     * @return respuesta HTTP 201 (Created) con la solicitud creada y su ID asignado
     */
    @PostMapping("/solicitudes")
    public ResponseEntity<SolicitudAfiliacion> registrarSolicitud(
            @Valid @RequestBody SolicitudAfiliacion solicitud) {

        SolicitudAfiliacion creada = snsService.registrarSolicitud(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * Consulta una solicitud de afiliacion especifica por su identificador.
     *
     * @param id identificador unico de la solicitud
     * @return respuesta HTTP 200 con la solicitud si existe,
     *         o HTTP 404 (Not Found) si no se encuentra
     */
    @GetMapping("/solicitudes/{id}")
    public ResponseEntity<SolicitudAfiliacion> consultarSolicitud(@PathVariable Long id) {
        return snsService.consultarSolicitud(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Consulta todas las solicitudes de afiliacion filtradas por estado.
     *
     * @param estado estado de la solicitud a filtrar (PENDIENTE, APROBADA,
     *               RECHAZADA, EN_REVISION). Por defecto "PENDIENTE"
     * @return respuesta HTTP 200 con la lista de solicitudes que coinciden
     *         con el estado indicado
     */
    @GetMapping("/solicitudes")
    public ResponseEntity<List<SolicitudAfiliacion>> consultarPorEstado(
            @RequestParam(defaultValue = "PENDIENTE") SolicitudAfiliacion.EstadoSolicitud estado) {

        return ResponseEntity.ok(snsService.consultarPorEstado(estado));
    }

    /**
     * Actualiza el estado de una solicitud de afiliacion existente.
     *
     * <p>Permite cambiar el estado de una solicitud (por ejemplo, de PENDIENTE
     * a APROBADA o RECHAZADA) y agregar observaciones sobre la decision.
     * Tambien registra la fecha de respuesta automaticamente.</p>
     *
     * @param id   identificador unico de la solicitud a actualizar
     * @param body mapa JSON con las claves "estado" (obligatorio) y
     *             "observaciones" (opcional)
     * @return respuesta HTTP 200 con la solicitud actualizada
     * @throws IllegalArgumentException si la solicitud con el ID dado no existe
     */
    @PatchMapping("/solicitudes/{id}/estado")
    public ResponseEntity<SolicitudAfiliacion> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        // Se extrae el nuevo estado del cuerpo de la peticion y se convierte al enum
        SolicitudAfiliacion.EstadoSolicitud nuevoEstado =
                SolicitudAfiliacion.EstadoSolicitud.valueOf(body.get("estado"));
        // Las observaciones son opcionales; si no se envian, se usa cadena vacia
        String observaciones = body.getOrDefault("observaciones", "");

        SolicitudAfiliacion actualizada = snsService.actualizarEstado(id, nuevoEstado, observaciones);
        return ResponseEntity.ok(actualizada);
    }

    /**
     * Endpoint de verificacion de salud (health check) del servicio SNS.
     *
     * <p>Devuelve informacion basica del estado del servicio, incluyendo
     * nombre del servicio, nodo y direccion IP simulada. Utilizado por
     * el balanceador de carga y herramientas de monitoreo.</p>
     *
     * @return respuesta HTTP 200 con el estado del servicio en formato JSON
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
