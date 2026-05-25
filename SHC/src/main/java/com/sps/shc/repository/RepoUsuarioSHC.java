package com.sps.shc.repository;

import com.sps.shc.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link Usuario}.
 *
 * <p>Proporciona operaciones CRUD estandar y una consulta derivada
 * para buscar un usuario por su cedula. Utilizado por
 * {@link com.sps.shc.service.SrvSHC} para implementar el patron
 * find-or-create al procesar eventos de compra terminada.</p>
 *
 * @author SPS Team
 * @see Usuario
 * @see com.sps.shc.service.SrvSHC
 */
public interface RepoUsuarioSHC extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su cedula (documento de identidad).
     *
     * @param cedula cedula del usuario a buscar
     * @return {@link Optional} con el usuario si existe, vacio si no
     */
    Optional<Usuario> findByCedula(String cedula);
}
