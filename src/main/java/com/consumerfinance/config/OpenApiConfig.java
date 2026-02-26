package com.consumerfinance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * OpenAPI/Swagger Configuration (T005)
 * 
 * Provides automatic API documentation via:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * - OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server")
                ))
                .info(new Info()
                        .title("Consumer Finance Loan Management System")
                        .version("1.0.0")
                        .description("REST API for personal loan management, EMI calculation, and loan repayment tracking. " +
                                "This API enables customers to register, apply for loans, calculate EMI, and track repayments. " +
                                "Admin endpoints support vendor management and application health monitoring.")
                        .contact(new Contact()
                                .name("Finance Development Team")
                                .url("https://github.com/your-repo")
                                .email("finance@example.com")
                        )
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")
                        )
                );
    }
}
