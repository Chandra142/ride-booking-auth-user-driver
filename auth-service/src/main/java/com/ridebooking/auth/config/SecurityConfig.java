package com.ridebooking.auth.config;

import com.ridebooking.auth.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ============================================================
 * SPRING SECURITY CONFIGURATION
 * ============================================================
 *
 * This class defines the security rules for the Auth Service.
 * Think of it as the security guard checkpoint for every request.
 *
 * KEY CONCEPTS:
 *
 * 1. SecurityFilterChain: A chain of filters that process requests.
 *    We customize which URLs require authentication and which don't.
 *
 * 2. PasswordEncoder: BCrypt is a slow, one-way hash function.
 *    When a user signs up, we hash their password with BCrypt.
 *    When they log in, we hash the input and compare hashes.
 *    We NEVER store the original password.
 *
 *    WHY BCrypt specifically?
 *    - It's "adaptive" — you can increase the work factor over time
 *      as computers get faster
 *    - It includes a random "salt" automatically, so two users with
 *      the same password get different hashes
 *    - It's intentionally SLOW (takes ~100ms) — this makes brute-force
 *      attacks impractical
 *
 * 3. SessionCreationPolicy.STATELESS:
 *    By default, Spring Security creates HTTP sessions (cookies).
 *    Since we use JWT tokens (not sessions), we tell Spring:
 *    "Don't create any session, don't use any session."
 *    Every request must have a valid JWT — there's no "logged-in"
 *    state on the server.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * ============================================================
     * SECURITY FILTER CHAIN
     * ============================================================
     *
     * Defines which URLs are public and which are protected.
     *
     * permitAll()   → anyone can access (no auth needed)
     * authenticated() → must have a valid JWT
     *
     * ORDER MATTERS: More specific paths should come before
     * wildcards (/**). Spring evaluates rules in order.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            /*
             * Disable CSRF (Cross-Site Request Forgery) protection.
             *
             * WHY? CSRF protection prevents attackers from tricking
             * logged-in users into making unwanted requests. But CSRF
             * relies on browser cookies/sessions. Since we're stateless
             * (JWT in Authorization header, not cookies), CSRF attacks
             * aren't possible. Disabling it avoids unnecessary complexity.
             */
            .csrf(csrf -> csrf.disable())

            /*
             * Define URL authorization rules.
             */
            .authorizeHttpRequests(auth -> auth
                /*
                 * Public endpoints — no authentication required.
                 * POST /api/v1/auth/register → anyone can sign up
                 * POST /api/v1/auth/login    → anyone can log in
                 * POST /api/v1/auth/refresh  → anyone can refresh (they have the refresh token)
                 * /h2-console/**            → H2 database console (dev only)
                 */
                .requestMatchers(
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/test",
                    "/h2-console/**"
                ).permitAll()

                /*
                 * Everything else requires authentication.
                 * The request must have a valid JWT in the
                 * Authorization header.
                 */
                .anyRequest().authenticated()
            )

            /*
             * STATELESS: Don't create or use HTTP sessions.
             * Each request is treated independently.
             */
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            /*
             * Allow H2 console to use frames (it uses them for the UI).
             * Spring Security blocks frames by default (clickjacking protection).
             */
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            /*
             * Add our JWT filter BEFORE Spring Security's
             * UsernamePasswordAuthenticationFilter.
             *
             * ORDER MATTERS IN THE FILTER CHAIN:
             * 1. Our filter extracts & validates the JWT
             * 2. Sets the Authentication in SecurityContext
             * 3. Spring's built-in filter reads SecurityContext
             *    and allows/denies based on our URL rules above
             *
             * If we added our filter AFTER, Spring would check
             * auth BEFORE we had a chance to set it — and would
             * reject every request.
             */
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * ============================================================
     * PASSWORD ENCODER
     * ============================================================
     *
     * Returns a BCryptPasswordEncoder bean that Spring Security
     * uses to hash and verify passwords automatically.
     *
     * BCrypt strength (10 rounds): 2^10 iterations = 1024 rounds.
     * Higher = more secure but slower. 10-12 is standard.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * ============================================================
     * AUTHENTICATION MANAGER
     * ============================================================
     *
     * The AuthenticationManager is the core interface that Spring
     * Security uses to authenticate users. We expose it as a bean
     * so we can inject it into our AuthService if needed.
     *
     * AuthenticationConfiguration is Spring Boot's auto-configured
     * version — we just need to call .getAuthenticationManager().
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
