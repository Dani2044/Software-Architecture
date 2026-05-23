package co.sps.sns.model;

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
 * <p><b>Rol en la arquitectura:</b> Capa de acceso a datos del simulador SNS.
 * Es utilizado por {@link co.sps.sns.service.SnsService} para consultar y
 * persistir solicitudes de afiliacion en la base de datos H2. El metodo
 * {@code findByNumeroDocumento} es clave para la validacion que MS-Compra
 * solicita via WebClient.</p>
 *
 * @author SPS Team
 * @version 1.0
 */
@Repository
public interface SolicitudAfiliacionRepository extends JpaRepository<SolicitudAfiliacion, Long> {

    /**
     * Busca una solicitud de afiliacion por el numero de documento del afiliado.
     *
     * <p>Metodo principal utilizado en el flujo de validacion: cuando MS-Compra
     * envia una peticion de validacion, el servicio usa este metodo para
     * verificar si el documento corresponde a un afiliado registrado.</p>
     *
     * @param numeroDocumento numero de documento a buscar
     * @return un {@link Optional} con la solicitud encontrada, o vacio si no existe
     */
    Optional<SolicitudAfiliacion> findByNumeroDocumento(String numeroDocumento);

    /**
     * Obtiene todas las solicitudes de afiliacion que coincidan con un estado dado.
     *
     * <p>Permite filtrar solicitudes por estado (PENDIENTE, APROBADA, RECHAZADA,
     * EN_REVISION) para consultas administrativas.</p>
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
     * @return {@code true} si existe al menos un registro con ese documento,
     *         {@code false} en caso contrario
     */
    boolean existsByNumeroDocumento(String numeroDocumento);
}
