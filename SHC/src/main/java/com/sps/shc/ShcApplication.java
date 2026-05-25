package com.sps.shc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del microservicio SHC (Sistema de Historias Clinicas).
 *
 * <p>Este microservicio forma parte de la arquitectura distribuida del sistema SPS
 * (Sistema de Compras en Salud). Su responsabilidad es escuchar eventos de compra
 * terminada a traves de una cola RabbitMQ ({@code ColaSHC}) y persistir los registros
 * correspondientes de historia clinica en la base de datos.</p>
 *
 * <p>Spring AMQP autoconfigura los listeners anotados con {@code @RabbitListener}
 * sin necesidad de anotacion adicional en la clase principal.</p>
 *
 * @see com.sps.shc.listener.ListenerSHC
 * @see com.sps.shc.service.SrvSHC
 */
@SpringBootApplication
public class ShcApplication {

    /**
     * Punto de entrada de la aplicacion Spring Boot.
     *
     * @param args argumentos de linea de comandos (opcionales)
     */
    public static void main(String[] args) {
        SpringApplication.run(ShcApplication.class, args);
    }
}
