package com.foodordering.menu.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI Configuration
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI menuServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Menu Service API")
                        .description("API Documentation cho Menu Service trong hệ thống Food Ordering")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Food Ordering Team")
                                .email("support@foodordering.com")));
    }
}
