package com.sps.authcatalogo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuracion de seguridad de Spring Security para el microservicio.
 *
 * <p>Decisiones de diseno:</p>
 * <ul>
 *   <li><b>Stateless:</b> no se crean sesiones HTTP; la autenticacion se delega
 *       al token JWT emitido por {@link com.sps.authcatalogo.auth.AuthController}.</li>
 *   <li><b>CSRF deshabilitado:</b> al ser una API REST sin estado, la proteccion CSRF
 *       no es necesaria; la seguridad se garantiza con el token Bearer.</li>
 *   <li><b>CORS permisivo:</b> permite cualquier origen para facilitar el consumo
 *       desde las SPAs del ecosistema SPS (SPS-SPA y SaludPay-SPA).</li>
 *   <li><b>Todos los endpoints abiertos:</b> este microservicio solo expone
 *       login, registro y consulta de catalogo; no requiere filtro JWT propio.</li>
 * </ul>
 *
 * @see com.sps.authcatalogo.auth.JwtService
 */
@Configuration
public class SecurityConfig {

    /**
     * Proveedor de codificacion de contrasenas usando BCrypt.
     *
     * <p>Se utiliza tanto para cifrar contrasenas al registrar usuarios
     * como para verificarlas durante el inicio de sesion.</p>
     *
     * @return instancia de {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura la politica CORS para permitir solicitudes desde cualquier origen.
     *
     * <p>Se permiten todos los metodos HTTP comunes y todas las cabeceras.
     * {@code allowCredentials} esta habilitado para soportar cookies/tokens
     * en solicitudes cross-origin.</p>
     *
     * @return fuente de configuracion CORS registrada para todas las rutas ({@code /**})
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        // Patron comodin para permitir cualquier origen (SPAs en desarrollo y produccion)
        cors.setAllowedOriginPatterns(List.of("*"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    /**
     * Define la cadena de filtros de seguridad HTTP.
     *
     * <p>Configuracion aplicada:</p>
     * <ol>
     *   <li>CORS con la fuente configurada en {@link #corsConfigurationSource()}.</li>
     *   <li>CSRF deshabilitado (API REST stateless).</li>
     *   <li>Gestion de sesiones en modo {@code STATELESS}.</li>
     *   <li>Todas las solicitudes se permiten sin autenticacion.</li>
     * </ol>
     *
     * @param http builder de configuracion de seguridad HTTP
     * @return cadena de filtros de seguridad construida
     * @throws Exception si ocurre un error durante la configuracion
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                // CSRF se deshabilita porque la API es stateless y usa tokens JWT
                .csrf(csrf -> csrf.disable())
                // Sin sesiones del lado del servidor; cada solicitud debe portar su propio token
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Todos los endpoints son publicos en este microservicio
                .authorizeHttpRequests(a -> a.anyRequest().permitAll());
        return http.build();
    }
}
