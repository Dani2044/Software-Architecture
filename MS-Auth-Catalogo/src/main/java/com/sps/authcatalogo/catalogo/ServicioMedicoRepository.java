package com.sps.authcatalogo.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para la entidad {@link ServicioMedico}.
 *
 * <p>Proporciona operaciones CRUD estandar heredadas de {@link JpaRepository}.
 * Utilizado por {@link CatalogoController} para listar todos los servicios
 * medicos disponibles y por {@link com.sps.authcatalogo.config.DataSeeder}
 * para la carga inicial de datos.</p>
 *
 * @see CatalogoController#servicios()
 */
public interface ServicioMedicoRepository extends JpaRepository<ServicioMedico, Long> {
}
