package co.sps.sns.service;

import co.sps.sns.model.SolicitudAfiliacion;
import co.sps.sns.model.RepoSNS;
import co.sps.sns.model.ValidacionAfiliado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de logica de negocio del simulador SNS (Superintendencia Nacional de Salud).
 *
 * <p>Contiene la logica central para validar afiliaciones al sistema de salud,
 * registrar nuevas solicitudes, consultarlas y actualizar su estado. Este servicio
 * es invocado por {@link co.sps.sns.controller.WSSNS}.</p>
 *
 * <p><b>Rol en la arquitectura:</b> Capa de servicio que implementa las reglas
 * de negocio del simulador SNS. MS-Compra consume este servicio de forma asincrona
 * via WebClient (no via MOM/colas) para validar si un usuario puede adquirir un
 * plan de salud. Las posibles respuestas de validacion son:</p>
 * <ul>
 *   <li><b>APROBADO:</b> El afiliado tiene estado APROBADA en la base de datos</li>
 *   <li><b>RECHAZADO:</b> El afiliado no fue encontrado o no tiene afiliacion activa</li>
 *   <li><b>ENPROCESO:</b> Representado por solicitudes en estado PENDIENTE o EN_REVISION</li>
 * </ul>
 *
 * @author SPS Team
 * @version 1.0
 */
@Service
public class SrvSNS {

    private static final Logger log = LoggerFactory.getLogger(SrvSNS.class);

    private final RepoSNS repository;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param repository repositorio JPA para acceso a datos de solicitudes de afiliacion
     */
    public SrvSNS(RepoSNS repository) {
        this.repository = repository;
    }

    /**
     * Valida si un afiliado tiene un plan de salud activo en el sistema.
     *
     * <p>Este es el metodo principal del servicio, invocado cuando MS-Compra
     * realiza una llamada asincrona via WebClient al endpoint de validacion.
     * La logica de validacion es la siguiente:</p>
     * <ol>
     *   <li>Busca al afiliado por numero de documento en la base de datos</li>
     *   <li>Si existe y su estado es APROBADA, retorna una validacion positiva
     *       con los datos del afiliado (equivalente a APROBADO)</li>
     *   <li>Si no existe o su estado es diferente a APROBADA, retorna una
     *       validacion negativa (equivalente a RECHAZADO)</li>
     * </ol>
     *
     * @param numeroDocumento numero de documento del afiliado a validar
     * @param tipoDocumento   tipo de documento (CC, TI, CE)
     * @return objeto {@link ValidacionAfiliado} con el resultado de la validacion
     */
    public ValidacionAfiliado validarAfiliado(String numeroDocumento, String tipoDocumento) {
        log.info("Validando afiliado: {} - {}", tipoDocumento, numeroDocumento);

        // Busca la solicitud de afiliacion por numero de documento
        Optional<SolicitudAfiliacion> solicitud = repository.findByNumeroDocumento(numeroDocumento);

        // Verifica que exista el registro Y que su estado sea APROBADA (afiliacion activa)
        if (solicitud.isPresent() && solicitud.get().getEstado() == SolicitudAfiliacion.EstadoSolicitud.APROBADA) {
            SolicitudAfiliacion s = solicitud.get();
            // Retorna validacion positiva con datos del afiliado y regimen CONTRIBUTIVO
            return new ValidacionAfiliado(true, s.getNumeroDocumento(),
                    s.getNombreAfiliado(), s.getEps(), "CONTRIBUTIVO",
                    "Afiliado activo en el sistema de salud");
        }

        // Si no se encontro o el estado no es APROBADA, se retorna validacion negativa
        return new ValidacionAfiliado(false, numeroDocumento, null, null, null,
                "No se encontro afiliacion activa para el documento");
    }

    /**
     * Registra una nueva solicitud de afiliacion al sistema de salud.
     *
     * <p>La solicitud se crea siempre con estado PENDIENTE, independientemente
     * del estado que se envie en el cuerpo de la peticion. La fecha de creacion
     * se asigna automaticamente via el callback {@code @PrePersist} de la entidad.</p>
     *
     * @param solicitud datos de la solicitud de afiliacion a registrar
     * @return la solicitud persistida con su ID asignado y estado PENDIENTE
     */
    public SolicitudAfiliacion registrarSolicitud(SolicitudAfiliacion solicitud) {
        log.info("Registrando solicitud de afiliacion para: {}", solicitud.getNumeroDocumento());
        // Se fuerza el estado a PENDIENTE para toda nueva solicitud
        solicitud.setEstado(SolicitudAfiliacion.EstadoSolicitud.PENDIENTE);
        return repository.save(solicitud);
    }

    /**
     * Consulta una solicitud de afiliacion por su identificador unico.
     *
     * @param id identificador de la solicitud a consultar
     * @return un {@link Optional} con la solicitud si existe, o vacio si no se encuentra
     */
    public Optional<SolicitudAfiliacion> consultarSolicitud(Long id) {
        return repository.findById(id);
    }

    /**
     * Obtiene todas las solicitudes de afiliacion filtradas por un estado dado.
     *
     * @param estado el estado por el cual filtrar las solicitudes
     * @return lista de solicitudes que coinciden con el estado indicado
     */
    public List<SolicitudAfiliacion> consultarPorEstado(SolicitudAfiliacion.EstadoSolicitud estado) {
        return repository.findByEstado(estado);
    }

    /**
     * Actualiza el estado de una solicitud de afiliacion existente.
     *
     * <p>Busca la solicitud por ID, actualiza su estado, registra la fecha
     * de respuesta y agrega las observaciones proporcionadas. Si la solicitud
     * no existe, lanza una excepcion {@link IllegalArgumentException}.</p>
     *
     * @param id             identificador de la solicitud a actualizar
     * @param nuevoEstado    nuevo estado a asignar (APROBADA, RECHAZADA, EN_REVISION, etc.)
     * @param observaciones  comentarios sobre la decision tomada
     * @return la solicitud actualizada y persistida
     * @throws IllegalArgumentException si no existe una solicitud con el ID proporcionado
     */
    public SolicitudAfiliacion actualizarEstado(Long id, SolicitudAfiliacion.EstadoSolicitud nuevoEstado, String observaciones) {
        // Busca la solicitud o lanza excepcion si no existe
        SolicitudAfiliacion solicitud = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + id));

        // Actualiza el estado, la fecha de respuesta y las observaciones
        solicitud.setEstado(nuevoEstado);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setObservaciones(observaciones);

        log.info("Solicitud {} actualizada a estado: {}", id, nuevoEstado);
        return repository.save(solicitud);
    }
}
