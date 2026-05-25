package com.sps.sam.config;

import com.sps.sam.dto.CompraTerminadaSamDto;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuracion RabbitMQ para SAM.
 *
 * <p>Declara la cola {@code ColaSAM} como bean durable y configura el
 * {@link Jackson2JsonMessageConverter} con {@link DefaultClassMapper} para
 * mapear el {@code __TypeId__} recibido a la clase local {@link CompraTerminadaSamDto}.</p>
 *
 * <p>Se aceptan DOS valores de {@code __TypeId__}:</p>
 * <ul>
 *   <li>El nombre logico {@code CompraTerminadaSam} (cuando el publisher usa
 *       el mismo nombre logico configurado en su DefaultClassMapper).</li>
 *   <li>El FQN {@code com.sps.compra.messaging.CompraTerminadaSamEvento} (cuando
 *       el publisher no resuelve el nombre logico y manda el class name completo).</li>
 * </ul>
 *
 * <p>Tambien se confian los paquetes {@code com.sps.*} para permitir cualquier
 * resolucion por FQN sin lanzar {@code IllegalArgumentException}.</p>
 *
 * @see com.sps.sam.listener.ListenerSAM
 */
@Configuration
public class RabbitMQConfiguration {

    @Bean
    public Queue colaSam() {
        return new Queue("ColaSAM", true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper mapper = new DefaultClassMapper();
        Map<String, Class<?>> idMappings = new HashMap<>();
        // Nombre logico (cuando MS-Compra resuelve correctamente el mapping)
        idMappings.put("CompraTerminadaSam", CompraTerminadaSamDto.class);
        // Fallback por FQN (cuando MS-Compra publica el class name completo)
        idMappings.put("com.sps.compra.messaging.CompraTerminadaSamEvento",
                CompraTerminadaSamDto.class);
        mapper.setIdClassMapping(idMappings);
        // Confiar paquetes de SPS para que loadClass() no rechace FQNs
        mapper.setTrustedPackages("com.sps", "java.util", "java.lang");
        converter.setClassMapper(mapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
