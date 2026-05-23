package com.sps.compra.service;

import com.sps.compra.entity.EstadoCompra;
import com.sps.compra.repository.RepoCompra;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Reintenta la validacion SNS para compras que quedaron en EN_VALIDACION_SNS
 * (porque la SNS respondio ENPROCESO o estaba caida). Cumple la robustez exigida
 * por el enunciado ante caidas de red.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TimerSNS {

    private final RepoCompra repoCompra;
    private final SrvCompras srvCompras;

    @Scheduled(fixedDelayString = "${sns.polling-ms:15000}")
    public void reintentar() {
        var pendientes = repoCompra.findByEstado(EstadoCompra.EN_VALIDACION_SNS);
        if (pendientes.isEmpty()) return;
        log.info("Reintentando validacion SNS para {} compra(s)", pendientes.size());
        pendientes.forEach(c -> srvCompras.validarConSnsAsync(c.getId()));
    }
}
