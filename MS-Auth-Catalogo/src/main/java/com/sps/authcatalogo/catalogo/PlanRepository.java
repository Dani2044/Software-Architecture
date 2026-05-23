package com.sps.authcatalogo.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link Plan}.
 *
 * <p>Proporciona operaciones CRUD estandar y un metodo de consulta derivado
 * para buscar planes por su codigo de negocio, utilizado en el endpoint
 * {@code GET /api/catalogo/planes/{codigo}}.</p>
 *
 * @see CatalogoController
 */
public interface PlanRepository extends JpaRepository<Plan, Long> {

    /**
     * Busca un plan de salud por su codigo unico de negocio.
     *
     * @param codigo codigo del plan a buscar (ej. {@code "PLAN-BASICO-001"})
     * @return un {@link Optional} con el plan si existe, vacio en caso contrario
     */
    Optional<Plan> findByCodigo(String codigo);
}
