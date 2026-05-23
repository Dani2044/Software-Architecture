package com.sps.shc.service;

import com.sps.shc.dto.CompraTerminadaShcDto;
import com.sps.shc.entity.HistoriaClinica;
import com.sps.shc.repository.HistoriaClinicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para el registro de historias clínicas.
 *
 * <p>Procesa los eventos de compra terminada recibidos desde la cola JMS y
 * persiste los registros correspondientes en la base de datos. Implementa
 * idempotencia a nivel de negocio: antes de insertar cada registro, verifica
 * que la combinación {@code (numeroCompra, codigoPlan)} no exista previamente,
 * evitando duplicados ante re-entregas de mensajes por parte del broker.</p>
 *
 * <p>La operación completa se ejecuta dentro de una transacción para garantizar
 * la consistencia de los datos.</p>
 *
 * @see com.sps.shc.listener.ShcListener
 * @see com.sps.shc.repository.HistoriaClinicaRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HistoriaClinicaService {

    private final HistoriaClinicaRepository repository;

    /**
     * Registra las historias clínicas derivadas de un evento de compra terminada.
     *
     * <p>Por cada plan incluido en el evento se crea un registro individual de
     * {@link HistoriaClinica}, siempre que no exista previamente un registro con
     * la misma combinación de {@code numeroCompra} y {@code codigoPlan}
     * (control de idempotencia).</p>
     *
     * <p>Si el evento no contiene planes o datos de persona, el método retorna
     * sin realizar ninguna operación.</p>
     *
     * @param evento DTO con los datos de la compra terminada (persona y planes)
     */
    @Transactional
    public void registrarCompra(CompraTerminadaShcDto evento) {
        // Validación temprana: si faltan datos esenciales, no hay nada que registrar
        if (evento.getPlanes() == null || evento.getPersona() == null) return;

        evento.getPlanes().forEach(p -> {
            // Control de idempotencia: verificar si el plan ya fue registrado para esta compra
            if (repository.existsByNumeroCompraAndCodigoPlan(evento.getNumeroCompra(), p.getCodigo())) {
                log.info("Plan {} de compra {} ya registrado, se omite",
                        p.getCodigo(), evento.getNumeroCompra());
                return; // Continúa con el siguiente plan (forEach)
            }
            // Construir y persistir el nuevo registro de historia clínica
            repository.save(HistoriaClinica.builder()
                    .numeroCompra(evento.getNumeroCompra())
                    .cedula(evento.getPersona().getCedula())
                    .nombre(evento.getPersona().getNombre())
                    .correo(evento.getPersona().getCorreo())
                    .codigoPlan(p.getCodigo())
                    .nombrePlan(p.getNombre())
                    .precioPlan(p.getPrecio())
                    .build());
        });

        log.info("Historia clinica registrada para compra {} ({} planes)",
                evento.getNumeroCompra(), evento.getPlanes().size());
    }
}
