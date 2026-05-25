package co.sps.sns.repository;

import co.sps.sns.entity.SolicitudAfiliacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link SolicitudAfiliacion}.
 *
 * <p>Proporciona operaciones CRUD estandar heredadas de {@link JpaRepository}
 * y metodos de consulta personalizados derivados por convencion de nombres
 * de Spring Data JPA.</p>
 *
 * <p>El metodo {@code findByNumeroDocumento} es clave para la validacion
 * que MS-Compra solicita via WebClient.</p>
 *
 * @author SPS Team
 * @see SolicitudAfiliacion
 * @see co.sps.sns.service.SrvSNS
 */
@Repository
public interface RepoSNS extends JpaRepository<SolicitudAfiliacion, Long> {

    /**
     * Busca una solicitud de afiliacion por el numero de documento del afiliado.
     *
     * @param numeroDocumento numero de documento a buscar
     * @return un {@link Optional} con la solicitud encontrada, o vacio si no existe
     */
    Optional<SolicitudAfiliacion> findByNumeroDocumento(String numeroDocumento);

    /**
     * Obtiene todas las solicitudes de afiliacion que coincidan con un estado dado.
     *
     * @param estado el estado de solicitud por el cual filtrar
     * @return lista de solicitudes que coinciden con el estado indicado
     */
    List<SolicitudAfiliacion> findByEstado(SolicitudAfiliacion.EstadoSolicitud estado);

    /**
     * Verifica si existe al menos una solicitud de afiliacion con el numero
     * de documento indicado.
     *
     * @param numeroDocumento numero de documento a verificar
     * @return {@code true} si existe al menos un registro con ese documento
     */
    boolean existsByNumeroDocumento(String numeroDocumento);
}
