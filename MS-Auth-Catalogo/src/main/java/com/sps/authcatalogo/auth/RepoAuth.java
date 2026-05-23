package com.sps.authcatalogo.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link Usuario}.
 *
 * <p>Proporciona operaciones CRUD estandar heredadas de {@link JpaRepository}
 * y un metodo de consulta derivado para buscar usuarios por nombre de usuario,
 * utilizado durante el flujo de autenticacion (login y registro).</p>
 *
 * @see SrvAuth
 */
public interface RepoAuth extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username nombre de usuario a buscar
     * @return un {@link Optional} con el usuario si existe, vacio en caso contrario
     */
    Optional<Usuario> findByUsername(String username);
}
