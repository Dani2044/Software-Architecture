package com.sps.sam.config;

import com.sps.sam.dto.CompraTerminadaSamDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuracion JMS para SAM.
 *
 * Define el mismo "nombre logico" {@code CompraTerminadaSam} que MS-Compra
 * usa al publicar (ver {@code com.sps.compra.config.JmsConfig}). Asi cuando
 * llega un mensaje a {@code ColaSAM} con header {@code _type=CompraTerminadaSam},
 * Jackson lo deserializa a {@link CompraTerminadaSamDto} aunque la clase
 * publicada por MS-Compra tenga FQN distinto.
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
        typeIdMappings.put("CompraTerminadaSam", CompraTerminadaSamDto.class);
        // Compatibilidad con mensajes viejos que traen el FQN de MS-Compra
        typeIdMappings.put("com.sps.compra.messaging.CompraTerminadaSamEvento",
                CompraTerminadaSamDto.class);
        converter.setTypeIdMappings(typeIdMappings);

        return converter;
    }
}
