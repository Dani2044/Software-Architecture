package co.sps.sns.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudAfiliacionRepository extends JpaRepository<SolicitudAfiliacion, Long> {

    Optional<SolicitudAfiliacion> findByNumeroDocumento(String numeroDocumento);

    List<SolicitudAfiliacion> findByEstado(SolicitudAfiliacion.EstadoSolicitud estado);

    boolean existsByNumeroDocumento(String numeroDocumento);
}
