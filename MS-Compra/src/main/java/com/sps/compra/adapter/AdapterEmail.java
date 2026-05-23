package com.sps.compra.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Adapter HTTP hacia el servicio de Email.
 * Encapsula las llamadas WebClient REST.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdapterEmail {

    private final WebClient.Builder webClientBuilder;

    @Value("${email.base-url:http://10.43.101.18:8084}")
    private String emailBaseUrl;

    public void enviar(Map<String, Object> payload) {
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
            log.warn("Fallo al notificar al servicio Email: {}", ex.getMessage());
        }
    }
}
