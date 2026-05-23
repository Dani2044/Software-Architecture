package com.sps.authcatalogo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio MS-Auth-Catalogo.
 *
 * <p>Este microservicio forma parte del Sistema de Prestaciones de Salud (SPS) y
 * expone dos dominios funcionales:</p>
 * <ul>
 *   <li><b>Autenticacion:</b> registro e inicio de sesion de usuarios con generacion de tokens JWT.</li>
 *   <li><b>Catalogo:</b> consulta del catalogo de planes de salud y servicios medicos disponibles.</li>
 * </ul>
 *
 * @see com.sps.authcatalogo.auth.AuthController
 * @see com.sps.authcatalogo.catalogo.CatalogoController
 */
@SpringBootApplication
public class AuthCatalogoApplication {

    /**
     * Metodo principal que arranca el contexto de Spring Boot.
     *
     * @param args argumentos de linea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthCatalogoApplication.class, args);
    }
}
