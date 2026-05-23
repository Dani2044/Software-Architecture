package com.sps.shc.repository;

import com.sps.shc.entity.HistoriaClinica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad {@link HistoriaClinica}.
 *
 * <p>Proporciona operaciones CRUD estándar heredadas de {@link JpaRepository}
 * y consultas derivadas personalizadas para buscar registros de historia clínica
 * por cédula, número de compra, o verificar duplicados.</p>
 *
 * <p>El método {@link #existsByNumeroCompraAndCodigoPlan(Long, String)} es clave
 * para la idempotencia del servicio: permite verificar si un plan ya fue registrado
 * para una compra específica antes de insertar un nuevo registro.</p>
 *
 * @see com.sps.shc.entity.HistoriaClinica
 * @see com.sps.shc.service.SrvSHC
 */
public interface RepoSHC extends JpaRepository<HistoriaClinica, Long> {

    /**
     * Verifica si ya existe un registro de historia clínica para la combinación
     * de número de compra y código de plan dada.
     *
     * <p>Se utiliza como control de idempotencia para evitar duplicados cuando
     * un mismo mensaje JMS es procesado más de una vez.</p>
     *
     * @param numeroCompra número de la compra de origen
     * @param codigoPlan   código del plan de salud
     * @return {@code true} si ya existe un registro con esa combinación, {@code false} en caso contrario
     */
    boolean existsByNumeroCompraAndCodigoPlan(Long numeroCompra, String codigoPlan);

    /**
     * Busca todos los registros de historia clínica asociados a una cédula.
     *
     * @param cedula número de cédula del paciente/comprador
     * @return lista de historias clínicas del paciente (puede estar vacía)
     */
    List<HistoriaClinica> findByCedula(String cedula);

    /**
     * Busca todos los registros de historia clínica generados a partir de una compra.
     *
     * @param numeroCompra número de la compra de origen
     * @return lista de historias clínicas de la compra (puede estar vacía)
     */
    List<HistoriaClinica> findByNumeroCompra(Long numeroCompra);
}
