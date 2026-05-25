package com.sps.compra.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Adapter HTTP hacia SaludPay-Back (.NET).
 * Encapsula las llamadas WebClient REST.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdapterSaludPay {

    private final WebClient.Builder webClientBuilder;

    @Value("${saludpay.base-url:http://10.43.100.111:5000}")
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
