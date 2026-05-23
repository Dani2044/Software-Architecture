package com.sps.compra.service;

import com.sps.compra.dto.CrearCompraRequest;
import com.sps.compra.entity.*;
import com.sps.compra.messaging.CompraTerminadaSamEvento;
import com.sps.compra.messaging.CompraTerminadaShcEvento;
import com.sps.compra.messaging.IntegraSAM;
import com.sps.compra.messaging.IntegraSHC;
import com.sps.compra.repository.RepoCompra;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SrvCompras {

    private final RepoCompra repoCompra;
    private final SrvSNS srvSNS;
    private final SrvEmail srvEmail;
    private final SrvSaludPay srvSaludPay;
    private final IntegraSAM integraSAM;
    private final IntegraSHC integraSHC;

    /**
     * Paso 1 del enunciado: persiste la compra y responde 202 al cliente.
     * La validacion contra SNS se dispara en background (no bloquea al cliente).
     */
    @Transactional
    public Compra crearCompra(CrearCompraRequest req) {
        Compra compra = Compra.builder()
                .cedulaCliente(req.getCedulaCliente())
                .nombreCliente(req.getNombreCliente())
                .correoCliente(req.getCorreoCliente())
                .valorTotal(calcularTotal(req))
                .estado(EstadoCompra.CREADA)
                .build();

        req.getPlanes().forEach(p -> {
            PlanSalud plan = PlanSalud.builder()
                    .compra(compra)
                    .codigoPlan(p.getCodigo())
                    .nombrePlan(p.getNombre())
                    .precio(p.getPrecio() != null ? p.getPrecio() : BigDecimal.ZERO)
                    .estadoSns(ValidacionSNS.ENPROCESO)
                    .build();
            if (p.getServicios() != null) {
                p.getServicios().forEach(s -> plan.getServicios().add(
                        ServicioMedico.builder()
                                .codigo(s.getCodigo())
                                .nombre(s.getNombre())
                                .duracionMinutos(s.getDuracionMinutos())
                                .build()));
            }
            compra.getPlanes().add(plan);
        });

        Compra guardada = repoCompra.save(compra);
        log.info("Compra creada id={} cedula={} total={}",
                guardada.getId(), guardada.getCedulaCliente(), guardada.getValorTotal());

        // Disparar validacion SNS de forma asincrona (no bloquea el HTTP del cliente)
        validarConSnsAsync(guardada.getId());

        return guardada;
    }

    private BigDecimal calcularTotal(CrearCompraRequest req) {
        return req.getPlanes().stream()
                .map(p -> p.getPrecio() != null ? p.getPrecio() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Recorre los planes y consulta la SNS. Si todos APROBADOS -> marca APROBADA y notifica
     * por correo + SaludPay. Si alguno RECHAZADO -> RECHAZADA. Si alguno ENPROCESO -> se queda
     * en EN_VALIDACION_SNS y el TimerSNS la reintentara mas tarde.
     */
    @Transactional
    public void validarConSnsAsync(Long compraId) {
        Compra compra = repoCompra.findById(compraId).orElse(null);
        if (compra == null) return;

        compra.setEstado(EstadoCompra.EN_VALIDACION_SNS);
        repoCompra.save(compra);

        boolean algunoEnProceso = false;
        boolean algunoRechazado = false;

        for (PlanSalud plan : compra.getPlanes()) {
            if (plan.getEstadoSns() == ValidacionSNS.APROBADO) continue;
            ValidacionSNS resp = srvSNS.validarAfiliado(plan.getCodigoPlan())
                    .onErrorReturn(ValidacionSNS.ENPROCESO)
                    .block();
            plan.setEstadoSns(resp);
            if (resp == ValidacionSNS.RECHAZADO) algunoRechazado = true;
            if (resp == ValidacionSNS.ENPROCESO) algunoEnProceso = true;
        }

        if (algunoRechazado) {
            compra.setEstado(EstadoCompra.RECHAZADA);
            compra.setObservacionSns("Uno o mas planes fueron rechazados por la SNS");
            repoCompra.save(compra);
            log.info("Compra {} RECHAZADA por SNS", compra.getId());
            return;
        }
        if (algunoEnProceso) {
            log.info("Compra {} queda en EN_VALIDACION_SNS, se reintentara", compra.getId());
            repoCompra.save(compra);
            return;
        }

        // Todos APROBADOS
        compra.setEstado(EstadoCompra.APROBADA);
        repoCompra.save(compra);
        log.info("Compra {} APROBADA por SNS", compra.getId());

        srvEmail.enviarCorreoAprobacion(
                compra.getCorreoCliente(), compra.getId(), compra.getValorTotal());
        srvSaludPay.publicarCompraPendiente(
                compra.getCedulaCliente(), compra.getId(), compra.getValorTotal());
    }

    /**
     * Llamado por ListenerPagos cuando llega un mensaje en ColaPagoConfirmado.
     */
    @Transactional
    public void marcarComoPagada(Long numeroCompra, BigDecimal valorPagado) {
        Compra compra = repoCompra.findById(numeroCompra).orElse(null);
        if (compra == null) {
            log.warn("Pago recibido para compra inexistente {}", numeroCompra);
            return;
        }
        if (compra.getEstado() == EstadoCompra.PAGADA
                || compra.getEstado() == EstadoCompra.TERMINADA) {
            log.info("Compra {} ya estaba {} (idempotente)",
                    compra.getId(), compra.getEstado());
            return;
        }
        compra.setEstado(EstadoCompra.PAGADA);
        repoCompra.save(compra);

        // Notificar SAM y SHC via MOM, marcar TERMINADA y avisar al correo
        integraSAM.publicar(buildEventoSam(compra));
        integraSHC.publicar(buildEventoShc(compra));

        compra.setEstado(EstadoCompra.TERMINADA);
        repoCompra.save(compra);

        srvEmail.enviarCorreoCompraTerminada(compra.getCorreoCliente(), compra.getId());
        log.info("Compra {} TERMINADA tras pago", compra.getId());
    }

    private CompraTerminadaSamEvento buildEventoSam(Compra compra) {
        List<CompraTerminadaSamEvento.ServicioMedicoMsg> servicios = compra.getPlanes().stream()
                .flatMap(p -> p.getServicios().stream())
                .map(s -> CompraTerminadaSamEvento.ServicioMedicoMsg.builder()
                        .codigo(s.getCodigo())
                        .nombre(s.getNombre())
                        .duracionMinutos(s.getDuracionMinutos())
                        .build())
                .toList();
        return CompraTerminadaSamEvento.builder()
                .numeroCompra(compra.getId())
                .cedulaCliente(compra.getCedulaCliente())
                .servicios(servicios)
                .build();
    }

    private CompraTerminadaShcEvento buildEventoShc(Compra compra) {
        List<CompraTerminadaShcEvento.PlanMsg> planes = compra.getPlanes().stream()
                .map(p -> CompraTerminadaShcEvento.PlanMsg.builder()
                        .codigo(p.getCodigoPlan())
                        .nombre(p.getNombrePlan())
                        .precio(p.getPrecio().doubleValue())
                        .build())
                .toList();
        return CompraTerminadaShcEvento.builder()
                .numeroCompra(compra.getId())
                .persona(CompraTerminadaShcEvento.PersonaMsg.builder()
                        .cedula(compra.getCedulaCliente())
                        .nombre(compra.getNombreCliente())
                        .correo(compra.getCorreoCliente())
                        .build())
                .planes(planes)
                .build();
    }
}
