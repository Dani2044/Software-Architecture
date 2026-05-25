package co.sps.sns.service;

import co.sps.sns.dto.ValidacionAfiliado;
import co.sps.sns.entity.PlanSalud;
import co.sps.sns.entity.SolicitudAfiliacion;
import co.sps.sns.repository.RepoEmpresa;
import co.sps.sns.repository.RepoPlanSalud;
import co.sps.sns.repository.RepoSNS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de logica de negocio del simulador SNS (Superintendencia Nacional de Salud).
 *
 * <p>Contiene la logica central para validar planes de salud, validar afiliaciones,
 * registrar nuevas solicitudes, consultarlas y actualizar su estado. Este servicio
 * es invocado por {@link co.sps.sns.controller.WSSNS}.</p>
 *
 * <p><b>Rol en la arquitectura:</b> Capa de servicio que implementa las reglas
 * de negocio del simulador SNS. MS-Compra consume este servicio de forma asincrona
 * via WebClient (Spring WebFlux) para validar si un plan puede ser vendido.
 * Las posibles respuestas de validacion son:</p>
 * <ul>
 *   <li><b>APROBADO:</b> El plan existe en el catalogo de la SNS y la aseguradora
 *       esta registrada</li>
 *   <li><b>RECHAZADO:</b> El plan no fue encontrado o la aseguradora no coincide</li>
 *   <li><b>ENPROCESO:</b> El plan esta en revision (simulado para reintentos)</li>
 * </ul>
 *
 * @author SPS Team
 */
@Service
public class SrvSNS {

    private static final Logger log = LoggerFactory.getLogger(SrvSNS.class);

    private final RepoSNS repository;
    private final RepoPlanSalud repoPlanSalud;
    private final RepoEmpresa repoEmpresa;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param repository     repositorio de solicitudes de afiliacion
     * @param repoPlanSalud  repositorio de planes de salud catalogados
     * @param repoEmpresa    repositorio de empresas aseguradoras (EPS)
     */
    public SrvSNS(RepoSNS repository, RepoPlanSalud repoPlanSalud, RepoEmpresa repoEmpresa) {
        this.repository = repository;
        this.repoPlanSalud = repoPlanSalud;
        this.repoEmpresa = repoEmpresa;
    }

    /**
     * Valida si un plan de salud puede ser vendido por la aseguradora.
     *
     * <p>Consulta la tabla {@code planes_salud} para verificar que el plan
     * existe en el catalogo de la SNS. Si se proporciona un codigo de
     * aseguradora, tambien verifica que la EPS este registrada y que
     * el plan pertenezca a esa aseguradora.</p>
     *
     * <p>Caso especial para demo: si el codigo contiene "PEND", retorna
     * ENPROCESO para probar la logica de reintentos del TimerSNS en MS-Compra.</p>
     *
     * @param codigoPlan        codigo del plan a validar
     * @param codigoAseguradora codigo de la aseguradora que solicita (puede ser null)
     * @return uno de los strings APROBADO, RECHAZADO o ENPROCESO
     */
    public String validarPlan(String codigoPlan, String codigoAseguradora) {
        log.info("Validando plan {} para aseguradora {}", codigoPlan, codigoAseguradora);

        if (codigoPlan == null || codigoPlan.isBlank()) return "RECHAZADO";

        // Hook de demo: simular estado ENPROCESO para probar reintentos
        if (codigoPlan.toUpperCase().contains("PEND")) return "ENPROCESO";

        // Consultar si el plan existe en el catalogo de la SNS
        Optional<PlanSalud> plan = repoPlanSalud.findByCodigo(codigoPlan);
        if (plan.isEmpty()) {
            log.info("Plan {} no encontrado en catalogo SNS", codigoPlan);
            return "RECHAZADO";
        }

        // Si se proporciono codigo de aseguradora, verificar que coincida
        if (codigoAseguradora != null && !codigoAseguradora.isBlank()) {
            // Verificar que el plan pertenece a esa aseguradora
            if (!codigoAseguradora.equals(plan.get().getCodigoAseguradora())) {
                log.info("Plan {} no pertenece a aseguradora {}", codigoPlan, codigoAseguradora);
                return "RECHAZADO";
            }
            // Verificar que la aseguradora esta registrada ante la SNS
            if (repoEmpresa.findByCodigoAseguradora(codigoAseguradora).isEmpty()) {
                log.info("Aseguradora {} no registrada ante la SNS", codigoAseguradora);
                return "RECHAZADO";
            }
        }

        log.info("Plan {} APROBADO", codigoPlan);
        return "APROBADO";
    }

    /**
     * Valida si un afiliado tiene un plan de salud activo en el sistema.
     *
     * @param numeroDocumento numero de documento del afiliado a validar
     * @param tipoDocumento   tipo de documento (CC, TI, CE)
     * @return objeto {@link ValidacionAfiliado} con el resultado de la validacion
     */
    public ValidacionAfiliado validarAfiliado(String numeroDocumento, String tipoDocumento) {
        log.info("Validando afiliado: {} - {}", tipoDocumento, numeroDocumento);

        Optional<SolicitudAfiliacion> solicitud = repository.findByNumeroDocumento(numeroDocumento);

        if (solicitud.isPresent() && solicitud.get().getEstado() == SolicitudAfiliacion.EstadoSolicitud.APROBADA) {
            SolicitudAfiliacion s = solicitud.get();
            return new ValidacionAfiliado(true, s.getNumeroDocumento(),
                    s.getNombreAfiliado(), s.getEps(), "CONTRIBUTIVO",
                    "Afiliado activo en el sistema de salud");
        }

        return new ValidacionAfiliado(false, numeroDocumento, null, null, null,
                "No se encontro afiliacion activa para el documento");
    }

    /**
     * Registra una nueva solicitud de afiliacion al sistema de salud.
     *
     * @param solicitud datos de la solicitud de afiliacion a registrar
     * @return la solicitud persistida con su ID asignado y estado PENDIENTE
     */
    public SolicitudAfiliacion registrarSolicitud(SolicitudAfiliacion solicitud) {
        log.info("Registrando solicitud de afiliacion para: {}", solicitud.getNumeroDocumento());
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
     * @param id             identificador de la solicitud a actualizar
     * @param nuevoEstado    nuevo estado a asignar
     * @param observaciones  comentarios sobre la decision tomada
     * @return la solicitud actualizada y persistida
     * @throws IllegalArgumentException si no existe una solicitud con el ID proporcionado
     */
    public SolicitudAfiliacion actualizarEstado(Long id, SolicitudAfiliacion.EstadoSolicitud nuevoEstado, String observaciones) {
        SolicitudAfiliacion solicitud = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + id));

        solicitud.setEstado(nuevoEstado);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setObservaciones(observaciones);

        log.info("Solicitud {} actualizada a estado: {}", id, nuevoEstado);
        return repository.save(solicitud);
    }
}
