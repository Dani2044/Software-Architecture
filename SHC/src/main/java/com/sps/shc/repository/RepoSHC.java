package com.sps.shc.repository;

import com.sps.shc.entity.PlanSalud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad {@link PlanSalud}.
 *
 * <p>Proporciona operaciones CRUD estandar heredadas de {@link JpaRepository}
 * y consultas derivadas personalizadas para buscar planes adquiridos
 * por cedula del paciente, por numero de compra, o verificar duplicados.</p>
 *
 * <p>El metodo {@link #existsByNumeroCompraAndCodigo(Long, String)} es clave
 * para la idempotencia del servicio: permite verificar si un plan ya fue
 * registrado para una compra especifica antes de insertar un nuevo registro.</p>
 *
 * @author SPS Team
 * @see PlanSalud
 * @see com.sps.shc.service.SrvSHC
 */
public interface RepoSHC extends JpaRepository<PlanSalud, Long> {

    /**
     * Verifica si ya existe un registro de plan adquirido para la combinacion
     * de numero de compra y codigo de plan dados.
     *
     * <p>Se utiliza como control de idempotencia para evitar duplicados cuando
     * un mismo mensaje JMS es procesado mas de una vez.</p>
     *
     * @param numeroCompra numero de la compra de origen
     * @param codigo       codigo del plan de salud
     * @return {@code true} si ya existe un registro con esa combinacion,
     *         {@code false} en caso contrario
     */
    boolean existsByNumeroCompraAndCodigo(Long numeroCompra, String codigo);

    /**
     * Busca todos los planes adquiridos asociados a una cedula de paciente.
     *
     * @param cedulaPaciente numero de cedula del paciente
     * @return lista de planes adquiridos del paciente (puede estar vacia)
     */
    List<PlanSalud> findByCedulaPaciente(String cedulaPaciente);

    /**
     * Busca todos los planes adquiridos asociados a un numero de compra.
     *
     * @param numeroCompra numero de la compra de origen
     * @return lista de planes adquiridos en esa compra (puede estar vacia)
     */
    List<PlanSalud> findByNumeroCompra(Long numeroCompra);
}
