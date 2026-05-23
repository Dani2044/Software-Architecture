package com.sps.authcatalogo.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Servicio responsable de la generacion de tokens JWT firmados con HMAC-SHA.
 *
 * <p>Utiliza la libreria <i>jjwt</i> para construir tokens compactos que contienen
 * la identidad del usuario (subject = username) junto con claims personalizados
 * (cedula, nombre, correo). El token resultante es consumido por otros
 * microservicios del ecosistema SPS para validar la identidad del usuario.</p>
 *
 * <p>La clave secreta y el tiempo de expiracion se configuran mediante las
 * propiedades {@code jwt.secret} y {@code jwt.expiration-ms} respectivamente.</p>
 *
 * @see AuthController#login(AuthController.LoginRequest)
 */
@Service
public class JwtService {

    /** Clave secreta para la firma HMAC-SHA, inyectada desde application.properties. */
    @Value("${jwt.secret}")
    private String secret;

    /** Tiempo de expiracion del token en milisegundos (por defecto 1 hora = 3 600 000 ms). */
    @Value("${jwt.expiration-ms:3600000}")
    private long expirationMs;

    /**
     * Construye la clave simetrica HMAC-SHA a partir de la cadena secreta configurada.
     *
     * @return instancia de {@link SecretKey} lista para firmar o verificar tokens
     */
    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un token JWT firmado para el usuario proporcionado.
     *
     * <p>Estructura del token:</p>
     * <ul>
     *   <li><b>subject:</b> username del usuario</li>
     *   <li><b>claims personalizados:</b> cedula, nombre y correo (valores nulos se reemplazan por cadena vacia)</li>
     *   <li><b>iat:</b> fecha/hora actual de emision</li>
     *   <li><b>exp:</b> fecha/hora actual + {@code expirationMs}</li>
     * </ul>
     *
     * @param u entidad {@link Usuario} cuya informacion se incluira en el token
     * @return cadena JWT compacta firmada con HMAC-SHA
     */
    public String generate(Usuario u) {
        return Jwts.builder()
                .subject(u.getUsername())
                // Claims personalizados: datos del perfil del usuario para uso en el frontend
                .claims(Map.of(
                        "cedula", u.getCedula(),
                        "nombre", u.getNombre() == null ? "" : u.getNombre(),
                        "correo", u.getCorreo() == null ? "" : u.getCorreo()
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key())
                .compact();
    }
}
