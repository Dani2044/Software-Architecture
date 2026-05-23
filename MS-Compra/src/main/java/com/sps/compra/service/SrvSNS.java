package com.sps.compra.service;

import com.sps.compra.adapter.WebClientSNS;
import com.sps.compra.entity.ValidacionSNS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Servicio de negocio para validacion contra la SNS.
 * Delega las llamadas HTTP al WebClientSNS.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SrvSNS {

    private final WebClientSNS webClientSNS;

    public Mono<ValidacionSNS> validarAfiliado(String codigoPlan) {
        return webClientSNS.validarPlan(codigoPlan);
    }
}
