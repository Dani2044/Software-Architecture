package com.sps.sam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

/**
 * Clase principal del microservicio SAM (Sistema de Agenda Medica).
 *
 * <p>SAM forma parte del sistema distribuido SPS (Sistema de Compras de Salud).
 * Su responsabilidad es escuchar la cola JMS {@code ColaSAM} mediante ActiveMQ
 * y persistir las citas de servicios medicos asociadas a compras finalizadas.</p>
 *
 * <p>La anotacion {@link EnableJms} activa la infraestructura de Spring JMS
 * para que los listeners declarados con {@code @JmsListener} sean detectados
 * y registrados automaticamente al iniciar la aplicacion.</p>
 *
 * @author SPS Team
 * @see com.sps.sam.listener.ListenerSAM
 * @see com.sps.sam.config.JmsConfig
 */
@SpringBootApplication
@EnableJms
public class SamApplication {

    /**
     * Punto de entrada de la aplicacion Spring Boot.
     *
     * <p>Inicializa el contexto de Spring, levanta el servidor embebido,
     * configura la conexion JMS a ActiveMQ y comienza a consumir mensajes
     * de la cola configurada.</p>
     *
     * @param args argumentos de linea de comandos (pueden incluir propiedades
     *             de Spring Boot como {@code --server.port} o {@code --spring.activemq.broker-url})
     */
    public static void main(String[] args) {

        SpringApplication.run(SamApplication.class, args);

    }
}