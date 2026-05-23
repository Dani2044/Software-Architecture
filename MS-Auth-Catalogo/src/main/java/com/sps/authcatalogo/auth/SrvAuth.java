package com.sps.authcatalogo.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Servicio de autenticacion que encapsula la logica de login y registro.
 *
 * <p>Extrae la logica de negocio que anteriormente residia en {@link AuthController},
 * delegando el acceso a datos a {@link RepoAuth} y la generacion de tokens a
 * {@link JwtService}.</p>
 *
 * @see RepoAuth
 * @see JwtService
 * @see AuthController
 */
@Service
@RequiredArgsConstructor
public class SrvAuth {

    private final RepoAuth repoAuth;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;

    /**
     * Autentica a un usuario mediante username y contrasena.
     *
     * <p>Busca el usuario por username, verifica la contrasena contra el hash BCrypt
     * almacenado y, si las credenciales son validas, genera un token JWT junto con
     * los datos del perfil.</p>
     *
     * @param username nombre de usuario
     * @param password contrasena en texto plano
     * @return un {@link Optional} con un mapa conteniendo el token JWT y datos del usuario
     *         si las credenciales son validas, vacio en caso contrario
     */
    public Optional<Map<String, Object>> login(String username, String password) {
        return repoAuth.findByUsername(username)
                .filter(u -> encoder.matches(password, u.getPasswordHash()))
                .map(u -> Map.<String, Object>of(
                        "token", jwtService.generate(u),
                        "cedula", u.getCedula(),
                        "nombre", u.getNombre(),
                        "correo", u.getCorreo()
                ));
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p>Valida que el username no exista previamente, cifra la contrasena con BCrypt
     * y persiste la nueva entidad {@link Usuario}.</p>
     *
     * @param username nombre de usuario deseado (debe ser unico)
     * @param password contrasena en texto plano (se almacenara como hash BCrypt)
     * @param cedula   cedula o documento de identidad
     * @param nombre   nombre completo del usuario (puede ser null)
     * @param correo   correo electronico del usuario (puede ser null)
     * @return un {@link Optional} con el usuario creado si el registro fue exitoso,
     *         vacio si el username ya existe
     */
    public Optional<Usuario> register(String username, String password, String cedula,
                                       String nombre, String correo) {
        if (repoAuth.findByUsername(username).isPresent()) {
            return Optional.empty();
        }
        Usuario u = Usuario.builder()
                .username(username)
                .passwordHash(encoder.encode(password))
                .cedula(cedula)
                .nombre(nombre)
                .correo(correo)
                .build();
        return Optional.of(repoAuth.save(u));
    }
}
