package com.sps.authcatalogo.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * POJO/Value Object que representa una sesion de token JWT decodificado.
 *
 * <p>Contiene los datos principales extraidos de un token JWT tras su verificacion:
 * nombre de usuario (subject), cedula del titular y fecha de expiracion.</p>
 *
 * <p>Este objeto se utiliza para transportar la informacion de sesion del usuario
 * de forma tipada, evitando el manejo directo de claims del token en la capa
 * de negocio.</p>
 *
 * @see JwtService
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenSesion {

    /** Nombre de usuario (subject del JWT). */
    private String username;

    /** Cedula o documento de identidad del usuario (claim personalizado del JWT). */
    private String cedula;

    /** Fecha y hora de expiracion del token. */
    private Date expiration;
}
