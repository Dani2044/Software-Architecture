package co.sps.email.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionEmailRepository extends JpaRepository<NotificacionEmail, Long> {

    List<NotificacionEmail> findByNumeroCompra(String numeroCompra);

    List<NotificacionEmail> findByEstado(NotificacionEmail.EstadoEnvio estado);
}
