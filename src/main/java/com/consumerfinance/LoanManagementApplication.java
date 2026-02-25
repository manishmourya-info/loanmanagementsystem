package com.consumerfinance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Consumer Finance Loan Management System",
        version = "1.0.0",
        description = "REST API for personal loan management, EMI calculation, and loan repayment tracking",
        contact = @Contact(
            name = "Finance Development Team"
        )
    )
)
public class LoanManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanManagementApplication.class, args);
    }

}
