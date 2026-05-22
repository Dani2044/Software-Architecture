package co.sps.sns.service;

import co.sps.sns.model.SolicitudAfiliacion;
import co.sps.sns.model.SolicitudAfiliacionRepository;
import co.sps.sns.model.ValidacionAfiliado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SnsService {

    private static final Logger log = LoggerFactory.getLogger(SnsService.class);

    private final SolicitudAfiliacionRepository repository;

    public SnsService(SolicitudAfiliacionRepository repository) {
        this.repository = repository;
    }

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

    public SolicitudAfiliacion registrarSolicitud(SolicitudAfiliacion solicitud) {
        log.info("Registrando solicitud de afiliacion para: {}", solicitud.getNumeroDocumento());
        solicitud.setEstado(SolicitudAfiliacion.EstadoSolicitud.PENDIENTE);
        return repository.save(solicitud);
    }

    public Optional<SolicitudAfiliacion> consultarSolicitud(Long id) {
        return repository.findById(id);
    }

    public List<SolicitudAfiliacion> consultarPorEstado(SolicitudAfiliacion.EstadoSolicitud estado) {
        return repository.findByEstado(estado);
    }

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
