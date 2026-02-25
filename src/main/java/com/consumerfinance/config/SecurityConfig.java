package com.consumerfinance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Configuration
 * Currently configured to allow all requests (bypass Spring Security for development/testing)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()  // Allow all requests without authentication
            )
            .csrf(csrf -> csrf.disable())  // Disable CSRF for development
            .httpBasic(basic -> basic.disable());  // Disable HTTP Basic authentication

        return http.build();
    }
}
