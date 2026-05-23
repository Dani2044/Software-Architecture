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
 * Cliente REST hacia SaludPay-Back (.NET).
 * Cuando SNS aprueba, MS-Compra publica la compra pendiente en SaludPay para que
 * el cliente pueda pagarla en la SPA de SaludPay.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SaludPayClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${saludpay.base-url:http://10.43.101.18:5000}")
    private String saludPayUrl;

    public void publicarCompraPendiente(String cedula, Long numeroCompra, BigDecimal valor) {
        try {
            webClientBuilder.baseUrl(saludPayUrl).build()
                    .post()
                    .uri("/api/compras-pendientes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "cedula", cedula,
                            "numeroCompra", numeroCompra,
                            "valor", valor
                    ))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Compra {} publicada en SaludPay para cedula {}", numeroCompra, cedula);
        } catch (Exception ex) {
            log.warn("No se pudo publicar compra en SaludPay: {}", ex.getMessage());
        }
    }
}
