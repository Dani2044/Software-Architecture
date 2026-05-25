package com.sps.compra.service;

import com.sps.compra.adapter.AdapterEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de negocio para envio de correos.
 * Delega las llamadas HTTP al AdapterEmail.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SrvEmail {

    private final AdapterEmail adapterEmail;

    @Value("${email.sps-url-pago:http://10.43.100.111:4201/pago}")
    private String urlPago;

    public void enviarCorreoAprobacion(String correo, Long numeroCompra, BigDecimal valor) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tipo", "APROBACION_SNS");
        payload.put("destinatario", correo);
        payload.put("numeroCompra", numeroCompra);
        payload.put("valor", valor);
        payload.put("urlPago", urlPago);
        adapterEmail.enviar(payload);
    }

    public void enviarCorreoCompraTerminada(String correo, Long numeroCompra) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tipo", "COMPRA_TERMINADA");
        payload.put("destinatario", correo);
        payload.put("numeroCompra", numeroCompra);
        adapterEmail.enviar(payload);
    }
}
