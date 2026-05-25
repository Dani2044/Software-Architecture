package com.sps.sam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del microservicio SAM (Sistema de Agenda Medica).
 *
 * <p>SAM forma parte del sistema distribuido SPS (Sistema de Compras de Salud).
 * Su responsabilidad es escuchar la cola {@code ColaSAM} mediante RabbitMQ
 * y persistir las citas de servicios medicos asociadas a compras finalizadas.</p>
 *
 * <p>Spring AMQP autoconfigura los listeners declarados con {@code @RabbitListener}
 * sin necesidad de anotacion adicional en la clase principal.</p>
 *
 * @author SPS Team
 * @see com.sps.sam.listener.ListenerSAM
 * @see com.sps.sam.config.RabbitMQConfiguration
 */
@SpringBootApplication
public class SamApplication {

    /**
     * Punto de entrada de la aplicacion Spring Boot.
     *
     * <p>Inicializa el contexto de Spring, levanta el servidor embebido,
     * configura la conexion AMQP a RabbitMQ y comienza a consumir mensajes
     * de la cola configurada.</p>
     *
     * @param args argumentos de linea de comandos (pueden incluir propiedades
     *             de Spring Boot como {@code --server.port} o {@code --spring.rabbitmq.host})
     */
    public static void main(String[] args) {
        SpringApplication.run(SamApplication.class, args);
    }
}
