package com.ridebooking.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ============================================================
 * AUTH SERVICE — ENTRY POINT
 * ============================================================
 *
 * Every Spring Boot application starts here. The @SpringBootApplication
 * annotation is shorthand for three annotations:
 *   1. @Configuration        — marks this as a source of bean definitions
 *   2. @EnableAutoConfiguration — Spring Boot auto-configures components
 *      based on what's in the classpath (e.g., if JPA is present, it
 *      auto-configures a DataSource and EntityManager)
 *   3. @ComponentScan        — tells Spring to scan THIS package and all
 *      sub-packages for @Component, @Service, @Repository, @Controller
 *
 * @EnableDiscoveryClient tells Spring Cloud to register this service
 * with the Eureka server (service registry) so the API Gateway can
 * discover and route traffic to it.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
