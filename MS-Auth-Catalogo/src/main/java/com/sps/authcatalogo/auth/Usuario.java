package com.sps.authcatalogo.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA que representa a un usuario del sistema SPS.
 *
 * <p>Almacena las credenciales de acceso (username + hash BCrypt) y datos
 * personales basicos (cedula, nombre, correo) que se incluyen como claims
 * en el token JWT tras un inicio de sesion exitoso.</p>
 *
 * <p>Lombok genera automaticamente getters, setters, equals, hashCode,
 * toString, builder y constructores.</p>
 *
 * @see JwtService#generate(Usuario)
 * @see AuthController
 */
@Entity
@Table(name = "usuario")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    /** Identificador unico auto-generado. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de usuario para autenticacion; debe ser unico en el sistema. */
    @Column(nullable = false, unique = true, length = 64)
    private String username;

    /** Hash BCrypt de la contrasena del usuario. Nunca se almacena en texto plano. */
    @Column(nullable = false, length = 200)
    private String passwordHash;

    /** Cedula de ciudadania o documento de identidad del usuario. */
    @Column(nullable = false, length = 32)
    private String cedula;

    /** Nombre completo del usuario (opcional). */
    @Column(length = 200)
    private String nombre;

    /** Correo electronico del usuario (opcional). */
    @Column(length = 200)
    private String correo;
}
