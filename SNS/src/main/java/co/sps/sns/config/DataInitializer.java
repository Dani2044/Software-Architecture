package co.sps.sns.config;

import co.sps.sns.entity.Empresa;
import co.sps.sns.entity.PlanSalud;
import co.sps.sns.entity.SolicitudAfiliacion;
import co.sps.sns.repository.RepoEmpresa;
import co.sps.sns.repository.RepoPlanSalud;
import co.sps.sns.repository.RepoSNS;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos de prueba para la base de datos del servicio SNS.
 *
 * <p>Se ejecuta automaticamente al arrancar la aplicacion mediante la interfaz
 * {@link CommandLineRunner}. Carga un conjunto predefinido de afiliados,
 * empresas aseguradoras y planes de salud de prueba.</p>
 *
 * @author SPS Team
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RepoSNS repoAfiliados;
    private final RepoEmpresa repoEmpresa;
    private final RepoPlanSalud repoPlanSalud;

    public DataInitializer(RepoSNS repoAfiliados, RepoEmpresa repoEmpresa, RepoPlanSalud repoPlanSalud) {
        this.repoAfiliados = repoAfiliados;
        this.repoEmpresa = repoEmpresa;
        this.repoPlanSalud = repoPlanSalud;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (repoAfiliados.count() > 0) return;

        // Afiliados aprobados: disponibles para validacion exitosa por MS-Compra
        crearAfiliado("1020304050", "CC", "Ana Torres",    "SURA",      SolicitudAfiliacion.EstadoSolicitud.APROBADA);
        crearAfiliado("9876543210", "CC", "Luis Perez",    "COMPENSAR", SolicitudAfiliacion.EstadoSolicitud.APROBADA);
        crearAfiliado("1122334455", "TI", "Maria Gomez",   "FAMISANAR", SolicitudAfiliacion.EstadoSolicitud.APROBADA);

        // Afiliado pendiente: simula estado ENPROCESO en validaciones
        crearAfiliado("5544332211", "CC", "Carlos Ruiz",   "NUEVA EPS", SolicitudAfiliacion.EstadoSolicitud.PENDIENTE);

        // Afiliado rechazado: simula estado RECHAZADO en validaciones
        crearAfiliado("6677889900", "CE", "Sofia Vargas",  "SURA",      SolicitudAfiliacion.EstadoSolicitud.RECHAZADA);

        // Empresas aseguradoras (EPS) registradas ante la SNS
        repoEmpresa.save(new Empresa("800123456-1", "SURA EPS",      "ASEG001"));
        repoEmpresa.save(new Empresa("800654321-2", "COMPENSAR EPS", "ASEG002"));

        // Planes de salud catalogados por la SNS
        repoPlanSalud.save(new PlanSalud("PLAN-BASICO",   "Plan Basico de Salud",   "ASEG001"));
        repoPlanSalud.save(new PlanSalud("PLAN-PREMIUM",  "Plan Premium de Salud",  "ASEG001"));

        log.info("DataInitializer: {} afiliados, {} empresas y {} planes cargados.",
                repoAfiliados.count(), repoEmpresa.count(), repoPlanSalud.count());
    }

    private void crearAfiliado(String documento, String tipo, String nombre,
                                String eps, SolicitudAfiliacion.EstadoSolicitud estado) {
        SolicitudAfiliacion s = new SolicitudAfiliacion();
        s.setNumeroDocumento(documento);
        s.setTipoDocumento(tipo);
        s.setNombreAfiliado(nombre);
        s.setEps(eps);
        s.setEstado(estado);
        repoAfiliados.save(s);
    }
}
