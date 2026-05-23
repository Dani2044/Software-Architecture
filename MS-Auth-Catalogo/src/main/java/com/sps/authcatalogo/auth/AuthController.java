package com.sps.authcatalogo.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST que expone los endpoints de autenticacion del sistema SPS.
 *
 * <p>Rutas disponibles bajo el prefijo {@code /api/auth}:</p>
 * <ul>
 *   <li>{@code POST /login}  - Inicio de sesion con username/password; retorna token JWT.</li>
 *   <li>{@code POST /register} - Registro de nuevos usuarios con contrasena cifrada BCrypt.</li>
 * </ul>
 *
 * <p>No se utiliza sesion del lado del servidor (stateless); la seguridad se
 * delega al token JWT que el cliente debe enviar en solicitudes posteriores
 * a otros microservicios.</p>
 *
 * <p>La logica de negocio se delega a {@link SrvAuth}.</p>
 *
 * @see SrvAuth
 * @see JwtService
 * @see com.sps.authcatalogo.config.SecurityConfig
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SrvAuth srvAuth;

    /**
     * Autentica a un usuario mediante username y contrasena.
     *
     * <p>Flujo:</p>
     * <ol>
     *   <li>Busca el usuario por username en la base de datos.</li>
     *   <li>Verifica la contrasena en texto plano contra el hash BCrypt almacenado.</li>
     *   <li>Si las credenciales son validas, genera un token JWT y lo retorna
     *       junto con los datos del perfil (cedula, nombre, correo).</li>
     *   <li>Si las credenciales son invalidas, retorna HTTP 401.</li>
     * </ol>
     *
     * @param req cuerpo de la solicitud con username y password
     * @return HTTP 200 con token JWT y datos del usuario, o HTTP 401 si las credenciales son invalidas
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return srvAuth.login(req.getUsername(), req.getPassword())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(401)
                        .body(Map.of("error", "Credenciales invalidas")));
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * <p>Flujo:</p>
     * <ol>
     *   <li>Valida que el username no exista previamente.</li>
     *   <li>Cifra la contrasena con BCrypt antes de persistirla.</li>
     *   <li>Crea y guarda la entidad {@link Usuario}.</li>
     * </ol>
     *
     * @param req cuerpo de la solicitud con username, password, cedula y datos opcionales
     * @return HTTP 200 con {@code {"status":"ok"}} si el registro fue exitoso,
     *         o HTTP 400 si el username ya existe
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        return srvAuth.register(req.getUsername(), req.getPassword(), req.getCedula(),
                        req.getNombre(), req.getCorreo())
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of("status", "ok")))
                .orElseGet(() -> ResponseEntity.badRequest()
                        .body(Map.of("error", "Username ya existe")));
    }

    /**
     * DTO interno para la solicitud de inicio de sesion.
     */
    @Data
    public static class LoginRequest {
        /** Nombre de usuario (obligatorio). */
        @NotBlank private String username;
        /** Contrasena en texto plano (obligatorio). */
        @NotBlank private String password;
    }

    /**
     * DTO interno para la solicitud de registro de un nuevo usuario.
     */
    @Data
    public static class RegisterRequest {
        /** Nombre de usuario deseado (obligatorio, debe ser unico). */
        @NotBlank private String username;
        /** Contrasena en texto plano (obligatorio); se almacenara como hash BCrypt. */
        @NotBlank private String password;
        /** Cedula o documento de identidad (obligatorio). */
        @NotBlank private String cedula;
        /** Nombre completo del usuario (opcional). */
        private String nombre;
        /** Correo electronico del usuario (opcional). */
        private String correo;
    }
}
