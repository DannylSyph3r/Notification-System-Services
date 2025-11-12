package dev.slethware.pushnotifications.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.queue.push}")
    private String pushQueueName;

    @Value("${rabbitmq.queue.failed}")
    private String failedQueueName;

    @Value("${rabbitmq.routing-key.push}")
    private String pushRoutingKey;

    @Value("${rabbitmq.routing-key.failed}")
    private String failedRoutingKey;

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue pushQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", exchangeName);
        args.put("x-dead-letter-routing-key", failedRoutingKey);
        args.put("x-message-ttl", 3600000); // 1 hour

        return QueueBuilder.durable(pushQueueName)
                .withArguments(args)
                .build();
    }

    @Bean
    public Queue failedQueue() {
        return QueueBuilder.durable(failedQueueName).build();
    }

    @Bean
    public Binding pushBinding(Queue pushQueue, DirectExchange exchange) {
        return BindingBuilder.bind(pushQueue).to(exchange).with(pushRoutingKey);
    }

    @Bean
    public Binding failedBinding(Queue failedQueue, DirectExchange exchange) {
        return BindingBuilder.bind(failedQueue).to(exchange).with(failedRoutingKey);
    }

    @Bean
    public ObjectMapper rabbitObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter(ObjectMapper rabbitObjectMapper) {
        return new Jackson2JsonMessageConverter(rabbitObjectMapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter producerJackson2MessageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(producerJackson2MessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(10);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}