package com.sps.sam.repository;

import com.sps.sam.entity.AgendaServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad {@link AgendaServicio}.
 *
 * <p>Proporciona operaciones CRUD estandar heredadas de {@link JpaRepository}
 * y consultas derivadas adicionales para las necesidades especificas de SAM:</p>
 * <ul>
 *   <li>Verificacion de existencia por compra y servicio (usada para idempotencia)</li>
 *   <li>Busqueda de agenda por cedula del paciente</li>
 *   <li>Busqueda de agenda por numero de compra</li>
 * </ul>
 *
 * @author SPS Team
 * @see AgendaServicio
 */
public interface RepoSAM extends JpaRepository<AgendaServicio, Long> {

    /**
     * Verifica si ya existe un registro con la combinacion de numero de compra
     * y codigo de servicio indicados.
     *
     * <p>Este metodo es clave para la <b>idempotencia</b> del proceso de registro:
     * antes de insertar un nuevo servicio, se consulta si la combinacion ya existe
     * para evitar duplicados en caso de reentrega de mensajes JMS.</p>
     *
     * @param numeroCompra  numero de la compra a verificar
     * @param codigoServicio codigo del servicio medico a verificar
     * @return {@code true} si ya existe un registro con esa combinacion,
     *         {@code false} en caso contrario
     */
    boolean existsByNumeroCompraAndCodigoServicio(Long numeroCompra, String codigoServicio);

    /**
     * Obtiene todos los servicios agendados para un paciente identificado
     * por su cedula.
     *
     * @param cedulaCliente cedula de identidad del paciente
     * @return lista de servicios agendados; vacia si no hay registros
     */
    List<AgendaServicio> findByCedulaCliente(String cedulaCliente);

    /**
     * Obtiene todos los servicios agendados asociados a un numero de compra
     * especifico.
     *
     * @param numeroCompra numero unico de la compra
     * @return lista de servicios agendados para esa compra; vacia si no hay registros
     */
    List<AgendaServicio> findByNumeroCompra(Long numeroCompra);
}
