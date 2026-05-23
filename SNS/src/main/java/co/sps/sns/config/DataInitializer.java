package co.sps.sns.config;

import co.sps.sns.model.Empresa;
import co.sps.sns.model.PlanSalud;
import co.sps.sns.model.SolicitudAfiliacion;
import co.sps.sns.model.RepoSNS;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos de prueba para la base de datos H2 del servicio SNS.
 *
 * <p>Se ejecuta automaticamente al arrancar la aplicacion mediante la interfaz
 * {@link CommandLineRunner}. Carga un conjunto predefinido de afiliados de prueba
 * con diferentes estados (APROBADA, PENDIENTE, RECHAZADA) para permitir la
 * simulacion de validaciones desde MS-Compra.</p>
 *
 * <p><b>Rol en la arquitectura:</b> Componente de configuracion que garantiza
 * que el simulador SNS tenga datos iniciales para responder a las consultas
 * de validacion de afiliacion realizadas por MS-Compra via WebClient.</p>
 *
 * @author SPS Team
 * @version 1.0
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RepoSNS repository;
    private final EntityManager entityManager;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param repository repositorio JPA para persistir solicitudes de afiliacion
     * @param entityManager entity manager para persistir entidades adicionales
     */
    public DataInitializer(RepoSNS repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    /**
     * Ejecuta la carga inicial de datos de prueba al arrancar la aplicacion.
     *
     * <p>Solo inserta datos si la tabla esta vacia, evitando duplicados en
     * reinicios sucesivos. Crea afiliados con distintos tipos de documento
     * (CC, TI, CE), diferentes EPS y variados estados de solicitud.</p>
     *
     * @param args argumentos de linea de comandos (no utilizados)
     */
    @Override
    @Transactional
    public void run(String... args) {
        // Si ya existen registros en la base de datos, no se vuelve a cargar
        if (repository.count() > 0) return;

        // Afiliados aprobados: disponibles para validacion exitosa por MS-Compra
        crearAfiliado("1020304050", "CC", "Ana Torres",    "SURA",      SolicitudAfiliacion.EstadoSolicitud.APROBADA);
        crearAfiliado("9876543210", "CC", "Luis Perez",    "COMPENSAR", SolicitudAfiliacion.EstadoSolicitud.APROBADA);
        crearAfiliado("1122334455", "TI", "Maria Gomez",   "FAMISANAR", SolicitudAfiliacion.EstadoSolicitud.APROBADA);

        // Afiliado pendiente: simula estado ENPROCESO en validaciones
        crearAfiliado("5544332211", "CC", "Carlos Ruiz",   "NUEVA EPS", SolicitudAfiliacion.EstadoSolicitud.PENDIENTE);

        // Afiliado rechazado: simula estado RECHAZADO en validaciones
        crearAfiliado("6677889900", "CE", "Sofia Vargas",  "SURA",      SolicitudAfiliacion.EstadoSolicitud.RECHAZADA);

        // Empresas aseguradoras (EPS) registradas ante la SNS
        entityManager.persist(new Empresa("800123456-1", "SURA EPS",      "ASEG001"));
        entityManager.persist(new Empresa("800654321-2", "COMPENSAR EPS", "ASEG002"));

        // Planes de salud catalogados por la SNS
        entityManager.persist(new PlanSalud("PLAN-BASICO",   "Plan Basico de Salud",   "ASEG001"));
        entityManager.persist(new PlanSalud("PLAN-PREMIUM",  "Plan Premium de Salud",  "ASEG001"));

        log.info("DataInitializer: {} afiliados, 2 empresas y 2 planes cargados.", repository.count());
    }

    /**
     * Metodo auxiliar que crea y persiste un registro de afiliado en la base de datos.
     *
     * @param documento numero de documento del afiliado
     * @param tipo      tipo de documento (CC, TI, CE)
     * @param nombre    nombre completo del afiliado
     * @param eps       nombre de la EPS a la que pertenece
     * @param estado    estado inicial de la solicitud de afiliacion
     */
    private void crearAfiliado(String documento, String tipo, String nombre,
                                String eps, SolicitudAfiliacion.EstadoSolicitud estado) {
        SolicitudAfiliacion s = new SolicitudAfiliacion();
        s.setNumeroDocumento(documento);
        s.setTipoDocumento(tipo);
        s.setNombreAfiliado(nombre);
        s.setEps(eps);
        s.setEstado(estado);
        repository.save(s);
    }
}
