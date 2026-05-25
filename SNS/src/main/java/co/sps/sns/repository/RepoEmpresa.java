package co.sps.sns.repository;

import co.sps.sns.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Empresa}.
 *
 * <p>Permite consultar las empresas aseguradoras (EPS) registradas
 * ante la SNS. Utilizado por {@link co.sps.sns.service.SrvSNS} para
 * verificar que la aseguradora que ofrece un plan esta autorizada.</p>
 *
 * @author SPS Team
 * @see Empresa
 */
@Repository
public interface RepoEmpresa extends JpaRepository<Empresa, Long> {

    /**
     * Busca una empresa aseguradora por su codigo de aseguradora.
     *
     * @param codigoAseguradora codigo de la aseguradora a buscar
     * @return un {@link Optional} con la empresa si existe, o vacio si no
     */
    Optional<Empresa> findByCodigoAseguradora(String codigoAseguradora);
}
