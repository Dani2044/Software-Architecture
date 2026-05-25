package com.sps.compra.config;

import com.sps.compra.messaging.CompraTerminadaSamEvento;
import com.sps.compra.messaging.CompraTerminadaShcEvento;
import com.sps.compra.messaging.TransaccionPago;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuracion JMS para MS-Compra.
 *
 * Usa nombres LOGICOS en el header {@code _type} en lugar del FQN de la
 * clase Java. Esto permite que MS-Compra, SAM y SHC tengan clases distintas
 * con el mismo "nombre logico" y se traduzcan correctamente al publicar y
 * consumir. Sin estos mappings, SAM y SHC fallan al intentar resolver el
 * FQN de las clases de MS-Compra (que no existen en su classpath).
 */
@Configuration
public class JmsConfig {

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        // Mapping logico class -> nombre que viaja en el header _type
        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("CompraTerminadaSam", CompraTerminadaSamEvento.class);
        typeIdMappings.put("CompraTerminadaShc", CompraTerminadaShcEvento.class);
        typeIdMappings.put("TransaccionPago", TransaccionPago.class);
        converter.setTypeIdMappings(typeIdMappings);

        return converter;
    }
}
