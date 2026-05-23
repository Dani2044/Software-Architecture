package com.sps.shc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

/**
 * Clase principal del microservicio SHC (Sistema de Historias Clínicas).
 *
 * <p>Este microservicio forma parte de la arquitectura distribuida del sistema SPS
 * (Sistema de Compras en Salud). Su responsabilidad es escuchar eventos de compra
 * terminada a través de una cola ActiveMQ ({@code ColaSHC}) y persistir los registros
 * correspondientes de historia clínica en la base de datos.</p>
 *
 * <p>La anotación {@link EnableJms} activa la infraestructura de Spring JMS,
 * permitiendo que los listeners anotados con {@code @JmsListener} consuman
 * mensajes de forma asíncrona.</p>
 *
 * @see com.sps.shc.listener.ShcListener
 * @see com.sps.shc.service.HistoriaClinicaService
 */
@SpringBootApplication
@EnableJms
public class ShcApplication {

    /**
     * Punto de entrada de la aplicación Spring Boot.
     *
     * @param args argumentos de línea de comandos (opcionales)
     */
    public static void main(String[] args) {
        SpringApplication.run(ShcApplication.class, args);
    }
}
