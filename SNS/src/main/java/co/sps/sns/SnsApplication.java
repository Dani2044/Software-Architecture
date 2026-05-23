package co.sps.sns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del microservicio SNS (Superintendencia Nacional de Salud).
 *
 * <p>Este microservicio actua como un simulador del servicio externo de la
 * Superintendencia Nacional de Salud. Su funcion principal es validar planes
 * de salud y gestionar solicitudes de afiliacion.</p>
 *
 * <p><b>Rol en la arquitectura:</b> Servicio externo invocado de forma asincrona
 * por MS-Compra a traves de WebClient (Spring WebFlux). No utiliza colas de
 * mensajeria (MOM). Las respuestas posibles de validacion son:
 * APROBADO, RECHAZADO o ENPROCESO.</p>
 *
 * @author SPS Team
 * @version 1.0
 */
@SpringBootApplication
public class SnsApplication {

    /**
     * Punto de entrada de la aplicacion Spring Boot.
     *
     * @param args argumentos de linea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(SnsApplication.class, args);
    }
}
