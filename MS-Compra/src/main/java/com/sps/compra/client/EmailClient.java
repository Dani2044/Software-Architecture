package com.sps.compra.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Cliente REST hacia el servicio Email.
 * El docx exige MOM solo entre MS-Compra y SAM/SHC; las notificaciones por correo
 * pueden hacerse via REST sincronico.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${email.base-url:http://10.43.100.122:8084}")
    private String emailBaseUrl;

    @Value("${email.sps-url-pago:http://10.43.101.18:4201/pago}")
    private String urlPago;

    public void enviarCorreoAprobacion(String correo, Long numeroCompra, BigDecimal valor) {
        var payload = Map.of(
                "tipo", "APROBACION_SNS",
                "destinatario", correo,
                "numeroCompra", numeroCompra,
                "valor", valor,
                "urlPago", urlPago
        );
        enviar(payload);
    }

    public void enviarCorreoCompraTerminada(String correo, Long numeroCompra) {
        var payload = Map.of(
                "tipo", "COMPRA_TERMINADA",
                "destinatario", correo,
                "numeroCompra", numeroCompra
        );
        enviar(payload);
    }

    private void enviar(Map<String, Object> payload) {
        try {
            webClientBuilder.baseUrl(emailBaseUrl).build()
                    .post()
                    .uri("/api/email/enviar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception ex) {
            // El email no es critico para el flujo: log y seguimos.
            log.warn("Fallo al notificar al servicio Email: {}", ex.getMessage());
        }
    }
}
