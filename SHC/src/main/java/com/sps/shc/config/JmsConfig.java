package com.sps.shc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * Configuración de JMS (Java Message Service) para el microservicio SHC.
 *
 * <p>Define el convertidor de mensajes que permite serializar y deserializar
 * objetos Java a/desde JSON al enviar y recibir mensajes a través de ActiveMQ.
 * Esto es esencial para que el {@link com.sps.shc.listener.ListenerSHC} pueda
 * recibir el DTO {@link com.sps.shc.dto.CompraTerminadaShcDto} directamente
 * como parámetro del método listener.</p>
 */
@Configuration
public class JmsConfig {

    /**
     * Crea y configura un convertidor de mensajes JMS basado en Jackson.
     *
     * <p>El convertidor se configura con las siguientes propiedades:</p>
     * <ul>
     *   <li><b>targetType = TEXT</b>: los mensajes se envían como {@code TextMessage}
     *       con contenido JSON, facilitando la interoperabilidad entre servicios.</li>
     *   <li><b>typeIdPropertyName = "_type"</b>: propiedad del mensaje JMS que indica
     *       el tipo de clase Java para la deserialización. Debe coincidir con la
     *       configuración del productor de mensajes (MS-Compra).</li>
     * </ul>
     *
     * @return instancia configurada de {@link MessageConverter}
     */
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        // Los mensajes se transmiten como texto JSON (no como bytes serializados)
        converter.setTargetType(MessageType.TEXT);
        // Propiedad JMS que identifica la clase destino para deserialización
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
