package com.bachhome.ai.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Cấu hình RestTemplate có load-balancing để gọi các service khác qua Eureka
 * (ví dụ http://SERVICE-PRODUCT/products).
 */
@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate();
    }
}
