package com.sps.authcatalogo.catalogo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Proxy/Facade que envuelve a {@link SrvCatalogo} para cumplir con el diagrama
 * de despliegue UML del sistema SPS.
 *
 * <p>Delega todas las llamadas al servicio subyacente sin agregar logica adicional.
 * Este componente existe como punto de extension para futuras funcionalidades
 * transversales (cache, logging, metricas) sin modificar el servicio principal.</p>
 *
 * @see SrvCatalogo
 */
@Component
@RequiredArgsConstructor
public class ProxyCatalogo {

    private final SrvCatalogo srvCatalogo;

    /**
     * Retorna la lista completa de planes de salud disponibles.
     *
     * @return lista de todos los {@link PlanSalud} registrados en el sistema
     */
    public List<PlanSalud> listarPlanes() {
        return srvCatalogo.listarPlanes();
    }

    /**
     * Busca un plan de salud especifico por su codigo de negocio.
     *
     * @param codigo codigo unico del plan
     * @return un {@link Optional} con el plan si existe, vacio en caso contrario
     */
    public Optional<PlanSalud> obtenerPlan(String codigo) {
        return srvCatalogo.obtenerPlan(codigo);
    }

    /**
     * Busca un plan de salud especifico por su identificador numerico.
     *
     * @param id identificador unico del plan
     * @return un {@link Optional} con el plan si existe, vacio en caso contrario
     */
    public Optional<PlanSalud> obtenerPlan(Long id) {
        return srvCatalogo.obtenerPlan(id);
    }

    /**
     * Retorna la lista completa de servicios medicos disponibles en el catalogo.
     *
     * @return lista de todos los {@link ServicioMedico} registrados en el sistema
     */
    public List<ServicioMedico> listarServicios() {
        return srvCatalogo.listarServicios();
    }
}
