package dev.slethware.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    @Value("${rabbitmq.queues.email}")
    private String emailQueueName;

    @Value("${rabbitmq.queues.push}")
    private String pushQueueName;

    @Value("${rabbitmq.queues.failed}")
    private String failedQueueName;

    @Value("${rabbitmq.routing-keys.email}")
    private String emailRoutingKey;

    @Value("${rabbitmq.routing-keys.push}")
    private String pushRoutingKey;

    @Value("${rabbitmq.routing-keys.failed}")
    private String failedRoutingKey;

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue emailQueue() {

        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", exchangeName);
        args.put("x-dead-letter-routing-key", failedRoutingKey);
        args.put("x-message-ttl", 3600000);

        return QueueBuilder.durable(emailQueueName)
                .withArguments(args)
                .build();
    }

    @Bean
    public Queue pushQueue() {

        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", exchangeName);
        args.put("x-dead-letter-routing-key", failedRoutingKey);
        args.put("x-message-ttl", 3600000);

        return QueueBuilder.durable(pushQueueName)
                .withArguments(args)
                .build();
    }

    @Bean
    public Queue failedQueue() {

        return QueueBuilder.durable(failedQueueName).build();
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange exchange) {
        return BindingBuilder.bind(emailQueue).to(exchange).with(emailRoutingKey);
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
    public Jackson2JsonMessageConverter producerJackson2MessageConverter(ObjectMapper redisObjectMapper) {

        return new Jackson2JsonMessageConverter(redisObjectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter producerJackson2MessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter);
        return rabbitTemplate;
    }
}