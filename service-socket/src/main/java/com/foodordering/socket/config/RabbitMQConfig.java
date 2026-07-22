package com.foodordering.socket.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "food-ordering-exchange";
    public static final String SOCKET_QUEUE = "socket-notification-queue";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue socketQueue() {
        return new Queue(SOCKET_QUEUE);
    }

    @Bean
    public Binding bindingOrderConfirmed(Queue socketQueue, TopicExchange exchange) {
        return BindingBuilder.bind(socketQueue).to(exchange).with("order.confirmed");
    }

    @Bean
    public Binding bindingOrderStatusChanged(Queue socketQueue, TopicExchange exchange) {
        return BindingBuilder.bind(socketQueue).to(exchange).with("order.status.changed");
    }

    @Bean
    public org.springframework.amqp.support.converter.MessageConverter converter() {
        return new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter();
    }
}
