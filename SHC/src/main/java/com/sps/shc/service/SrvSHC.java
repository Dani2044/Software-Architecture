package com.sps.shc.service;

import com.sps.shc.dto.CompraTerminadaShcDto;
import com.sps.shc.entity.PlanSalud;
import com.sps.shc.entity.Usuario;
import com.sps.shc.repository.RepoSHC;
import com.sps.shc.repository.RepoUsuarioSHC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de negocio para el registro de historias clinicas.
 *
 * <p>Procesa los eventos de compra terminada recibidos desde la cola JMS y
 * persiste los registros correspondientes en la base de datos utilizando
 * dos entidades: {@link Usuario} (datos del paciente) y {@link PlanSalud}
 * (planes adquiridos por compra).</p>
 *
 * <p>Implementa un patron de <b>registro idempotente</b>:</p>
 * <ol>
 *   <li><b>Usuario:</b> patron find-or-create por cedula. Si el paciente
 *       ya existe, se reutiliza; si no, se crea.</li>
 *   <li><b>PlanSalud:</b> antes de insertar, se verifica con
 *       {@link RepoSHC#existsByNumeroCompraAndCodigo} si la combinacion
 *       (numeroCompra, codigo) ya existe, evitando duplicados ante
 *       re-entregas de mensajes por parte del broker.</li>
 * </ol>
 *
 * <p>La operacion completa se ejecuta dentro de una transaccion unica
 * para garantizar la consistencia de los datos.</p>
 *
 * @author SPS Team
 * @see com.sps.shc.listener.ListenerSHC
 * @see com.sps.shc.repository.RepoSHC
 * @see com.sps.shc.repository.RepoUsuarioSHC
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SrvSHC {

    private final RepoSHC repoPlanes;
    private final RepoUsuarioSHC repoUsuarios;

    /**
     * Registra las historias clinicas derivadas de un evento de compra terminada.
     *
     * <p>Primero crea o recupera el {@link Usuario} asociado al paciente
     * (por cedula). Luego, por cada plan incluido en el evento, crea un
     * registro individual de {@link PlanSalud}, siempre que no exista
     * previamente un registro con la misma combinacion de
     * {@code numeroCompra} y {@code codigo} (control de idempotencia).</p>
     *
     * <p>Si el evento no contiene planes o datos de persona, el metodo
     * retorna sin realizar ninguna operacion.</p>
     *
     * @param evento DTO con los datos de la compra terminada (persona y planes)
     */
    @Transactional
    public void registrarCompra(CompraTerminadaShcDto evento) {
        // Validacion temprana: si faltan datos esenciales, no hay nada que registrar
        if (evento.getPlanes() == null || evento.getPersona() == null) return;

        // Find-or-create del usuario (paciente)
        CompraTerminadaShcDto.PersonaDto persona = evento.getPersona();
        Usuario usuario = repoUsuarios.findByCedula(persona.getCedula())
                .orElseGet(() -> {
                    log.info("Nuevo usuario registrado: cedula={}", persona.getCedula());
                    return repoUsuarios.save(Usuario.builder()
                            .cedula(persona.getCedula())
                            .nombre(persona.getNombre())
                            .correo(persona.getCorreo())
                            .build());
                });

        // Registrar cada plan adquirido con idempotencia
        evento.getPlanes().forEach(p -> {
            // Control de idempotencia: verificar si el plan ya fue registrado para esta compra
            if (repoPlanes.existsByNumeroCompraAndCodigo(evento.getNumeroCompra(), p.getCodigo())) {
                log.info("Plan {} de compra {} ya registrado, se omite",
                        p.getCodigo(), evento.getNumeroCompra());
                return; // Continua con el siguiente plan (forEach)
            }
            // Construir y persistir el nuevo registro de plan adquirido
            repoPlanes.save(PlanSalud.builder()
                    .numeroCompra(evento.getNumeroCompra())
                    .cedulaPaciente(usuario.getCedula())
                    .codigo(p.getCodigo())
                    .nombre(p.getNombre())
                    .precio(p.getPrecio())
                    .build());
        });

        log.info("Historia clinica registrada para compra {} ({} planes, usuario={})",
                evento.getNumeroCompra(), evento.getPlanes().size(), usuario.getCedula());
    }
}
