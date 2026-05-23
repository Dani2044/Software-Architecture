package com.sps.compra.service;

import com.sps.compra.entity.DatosClinicos;
import com.sps.compra.repository.RepoClinica;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio que coordina datos clinicos (DatosClinicos) para la integracion SHC.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SrvClinica {

    private final RepoClinica repoClinica;

    /**
     * Construye los datos clinicos a partir de la informacion del cliente.
     */
    public DatosClinicos construirDatosClinicos(String cedula, String nombre, String correo) {
        return DatosClinicos.builder()
                .cedula(cedula)
                .nombre(nombre)
                .correo(correo)
                .build();
    }
}
