package com.sps.shc.config;

import com.sps.shc.dto.CompraTerminadaShcDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuracion JMS para SHC.
 *
 * Mapea el nombre logico {@code CompraTerminadaShc} (que MS-Compra publica
 * en el header {@code _type}) a la clase local {@link CompraTerminadaShcDto}.
 */
@Configuration
public class JmsConfig {

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        // Nombre logico moderno
        typeIdMappings.put("CompraTerminadaShc", CompraTerminadaShcDto.class);
        // Compatibilidad con mensajes viejos que traen el FQN de MS-Compra
        typeIdMappings.put("com.sps.compra.messaging.CompraTerminadaShcEvento",
                CompraTerminadaShcDto.class);
        converter.setTypeIdMappings(typeIdMappings);

        return converter;
    }
}
