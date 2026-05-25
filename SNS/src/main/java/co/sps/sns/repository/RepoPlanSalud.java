package co.sps.sns.repository;

import co.sps.sns.entity.PlanSalud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link PlanSalud}.
 *
 * <p>Permite consultar los planes de salud catalogados por la SNS.
 * Utilizado por {@link co.sps.sns.service.SrvSNS} en el flujo de
 * validacion de planes solicitado por MS-Compra.</p>
 *
 * @author SPS Team
 * @see PlanSalud
 */
@Repository
public interface RepoPlanSalud extends JpaRepository<PlanSalud, Long> {

    /**
     * Busca un plan de salud por su codigo unico.
     *
     * @param codigo codigo del plan a buscar
     * @return un {@link Optional} con el plan si existe, o vacio si no
     */
    Optional<PlanSalud> findByCodigo(String codigo);
}
