package com.sps.authcatalogo.catalogo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de catalogo que encapsula la logica de consulta de planes y servicios medicos.
 *
 * <p>Extrae la logica de negocio delegando el acceso a datos a {@link RepoCatalogo}
 * y {@link ServicioMedicoRepository}. Es invocado por {@link ProxyCatalogo}.</p>
 *
 * @see RepoCatalogo
 * @see ServicioMedicoRepository
 * @see ProxyCatalogo
 */
@Service
@RequiredArgsConstructor
public class SrvCatalogo {

    private final RepoCatalogo repoCatalogo;
    private final ServicioMedicoRepository servicioRepository;

    /**
     * Retorna la lista completa de planes de salud disponibles.
     *
     * @return lista de todos los {@link PlanSalud} registrados en el sistema
     */
    public List<PlanSalud> listarPlanes() {
        return repoCatalogo.findAll();
    }

    /**
     * Busca un plan de salud especifico por su codigo de negocio.
     *
     * @param codigo codigo unico del plan (ej. {@code "PLAN-BASICO-001"})
     * @return un {@link Optional} con el plan si existe, vacio en caso contrario
     */
    public Optional<PlanSalud> obtenerPlan(String codigo) {
        return repoCatalogo.findByCodigo(codigo);
    }

    /**
     * Busca un plan de salud especifico por su identificador numerico.
     *
     * @param id identificador unico del plan
     * @return un {@link Optional} con el plan si existe, vacio en caso contrario
     */
    public Optional<PlanSalud> obtenerPlan(Long id) {
        return repoCatalogo.findById(id);
    }

    /**
     * Retorna la lista completa de servicios medicos disponibles en el catalogo.
     *
     * @return lista de todos los {@link ServicioMedico} registrados en el sistema
     */
    public List<ServicioMedico> listarServicios() {
        return servicioRepository.findAll();
    }
}
