package co.sps.sns.config;

import co.sps.sns.model.SolicitudAfiliacion;
import co.sps.sns.model.SolicitudAfiliacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final SolicitudAfiliacionRepository repository;

    public DataInitializer(SolicitudAfiliacionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;

        crearAfiliado("1020304050", "CC", "Ana Torres",    "SURA",      SolicitudAfiliacion.EstadoSolicitud.APROBADA);
        crearAfiliado("9876543210", "CC", "Luis Perez",    "COMPENSAR", SolicitudAfiliacion.EstadoSolicitud.APROBADA);
        crearAfiliado("1122334455", "TI", "Maria Gomez",   "FAMISANAR", SolicitudAfiliacion.EstadoSolicitud.APROBADA);
        crearAfiliado("5544332211", "CC", "Carlos Ruiz",   "NUEVA EPS", SolicitudAfiliacion.EstadoSolicitud.PENDIENTE);
        crearAfiliado("6677889900", "CE", "Sofia Vargas",  "SURA",      SolicitudAfiliacion.EstadoSolicitud.RECHAZADA);

        log.info("DataInitializer: {} afiliados de prueba cargados en H2.", repository.count());
    }

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
