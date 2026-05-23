package com.sps.sam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * Configuracion de la infraestructura JMS para el microservicio SAM.
 *
 * <p>Define el {@link MessageConverter} que Spring JMS utilizara para
 * serializar y deserializar los mensajes que transitan por ActiveMQ.
 * Se emplea Jackson (JSON) como formato de intercambio, lo que permite
 * que los DTOs se conviertan automaticamente desde/hacia mensajes de texto JMS.</p>
 *
 * <p>La propiedad {@code _type} se incluye en cada mensaje para que el
 * consumidor pueda resolver el tipo Java correcto durante la deserializacion,
 * garantizando la compatibilidad entre los distintos microservicios del SPS.</p>
 *
 * @author SPS Team
 * @see org.springframework.jms.support.converter.MappingJackson2MessageConverter
 */
@Configuration
public class JmsConfig {

    /**
     * Crea y configura el convertidor de mensajes JMS basado en Jackson.
     *
     * <p>Caracteristicas de la configuracion:</p>
     * <ul>
     *   <li><b>Tipo de mensaje:</b> {@link MessageType#TEXT} - los mensajes se
     *       envian como {@code TextMessage} con contenido JSON.</li>
     *   <li><b>Propiedad de tipo:</b> {@code _type} - se agrega como propiedad
     *       JMS al mensaje para que el receptor conozca la clase Java destino
     *       y pueda deserializar correctamente.</li>
     * </ul>
     *
     * @return instancia configurada de {@link MessageConverter} lista para
     *         ser utilizada por el {@code JmsTemplate} y los listeners JMS
     */
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        // Los mensajes se envian como texto JSON (TextMessage de JMS)
        converter.setTargetType(MessageType.TEXT);
        // Propiedad JMS que identifica el tipo Java para la deserializacion
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
