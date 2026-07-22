package com.foodordering.order.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "food-ordering-exchange";
    public static final String ORDER_CONFIRMED_ROUTING_KEY = "order.confirmed";
    public static final String PAYMENT_CONFIRMED_ROUTING_KEY = "payment.confirmed";
    public static final String PAYMENT_CONFIRMED_QUEUE = "payment.confirmed.order-service";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    /**
     * Queue Lịch sử (History):
     * - Non-durable: Sẽ mất khi restart RabbitMQ (giống như hết phiên).
     * - TTL 1 giờ: Message tự xóa sau 1 giờ.
     * - Max Length 100: Chỉ lưu 100 message gần nhất.
     */
    @Bean
    public Queue historyQueue() {
        java.util.Map<String, Object> args = new java.util.HashMap<>();
        args.put("x-message-ttl", 3600000); // 1 giờ (ms)
        args.put("x-max-length", 100);      // Lưu tối đa 100 message
        
        // name, durable, exclusive, autoDelete, arguments
        return new Queue("order.history.queue", false, false, false, args);
    }

    @Bean
    public Binding historyBinding(Queue historyQueue, TopicExchange exchange) {
        return BindingBuilder.bind(historyQueue).to(exchange).with(ORDER_CONFIRMED_ROUTING_KEY);
    }

    /**
     * Queue cho Payment Confirmed events
     * Order-service lắng nghe để cập nhật paymentStatus
     */
    @Bean
    public Queue paymentConfirmedQueue() {
        return new Queue(PAYMENT_CONFIRMED_QUEUE, true);
    }

    @Bean
    public Binding paymentConfirmedBinding(TopicExchange exchange) {
        return BindingBuilder.bind(paymentConfirmedQueue()).to(exchange).with(PAYMENT_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}

