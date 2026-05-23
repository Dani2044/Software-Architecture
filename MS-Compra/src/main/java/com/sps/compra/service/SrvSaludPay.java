package com.sps.compra.service;

import com.sps.compra.adapter.AdapterSaludPay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Servicio de negocio para integracion con SaludPay.
 * Delega las llamadas HTTP al AdapterSaludPay.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SrvSaludPay {

    private final AdapterSaludPay adapterSaludPay;

    public void publicarCompraPendiente(String cedula, Long numeroCompra, BigDecimal valor) {
        adapterSaludPay.publicarCompraPendiente(cedula, numeroCompra, valor);
    }
}
