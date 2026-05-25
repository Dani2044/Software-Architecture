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
 *
 * Construye el payload con los nombres EXACTOS que el servicio Email valida
 * en NotificacionRequest: correoCliente, nombreCliente, numeroCompra (String),
 * valorCompra (Double), tipo (enum del Email).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SrvEmail {

    private final AdapterEmail adapterEmail;

    @Value("${email.sps-url-pago:http://10.43.100.111:4201/pago}")
    private String urlPago;

    public void enviarCorreoAprobacion(String correo, String nombre, Long numeroCompra, BigDecimal valor) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tipo", "COMPRA_APROBADA_SNS");
        payload.put("correoCliente", correo);
        payload.put("nombreCliente", nombre != null ? nombre : "Cliente SPS");
        payload.put("numeroCompra", String.valueOf(numeroCompra));
        payload.put("valorCompra", valor != null ? valor.doubleValue() : 0.0);
        payload.put("urlPago", urlPago);
        adapterEmail.enviar(payload);
    }

    public void enviarCorreoCompraTerminada(String correo, String nombre, Long numeroCompra, BigDecimal valor) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tipo", "COMPRA_COMPLETADA");
        payload.put("correoCliente", correo);
        payload.put("nombreCliente", nombre != null ? nombre : "Cliente SPS");
        payload.put("numeroCompra", String.valueOf(numeroCompra));
        payload.put("valorCompra", valor != null ? valor.doubleValue() : 0.0);
        adapterEmail.enviar(payload);
    }
}
