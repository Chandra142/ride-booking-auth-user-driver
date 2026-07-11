package com.ridebooking.auth.security;

import com.ridebooking.common.constant.ServiceConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * ============================================================
 * JWT AUTHENTICATION FILTER
 * ============================================================
 *
 * This is the gatekeeper for every HTTP request.
 * Spring Security uses a chain of "filters" — each filter
 * examines the request and either allows it through or rejects it.
 *
 * FILTER CHAIN ORDER:
 * 1. Request comes in
 * 2. JwtAuthenticationFilter (us) → checks for JWT in Authorization header
 * 3. If valid → sets the authenticated user in SecurityContext
 * 4. Next filters → check if user has permission for this endpoint
 * 5. Controller → handles the actual business logic
 *
 * HOW IT WORKS:
 * Every HTTP request passes through doFilterInternal().
 * We extract the JWT from the "Authorization: Bearer <token>" header:
 * 1. Strip the "Bearer " prefix to get just the token
 * 2. Validate the token (signature, expiration)
 * 3. If valid, create an Authentication object and put it in
 *    SecurityContextHolder (Spring Security's storage for "who is logged in")
 * 4. The request continues down the chain as "authenticated"
 *
 * WHY OncePerRequestFilter?
 * Normal filters can be called multiple times in a request cycle
 * (e.g., if there are forwards/includes). This filter guarantees
 * it runs exactly ONCE per request.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Core filter logic — called for every request.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Extract the JWT from the Authorization header
        String token = extractTokenFromRequest(request);

        // Step 2: If token exists and is valid, authenticate the user
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

            // Extract claims from the token
            String userId = jwtTokenProvider.getUserIdFromToken(token);
            String email = jwtTokenProvider.getEmailFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            /*
             * Create a UsernamePasswordAuthenticationToken.
             * This is Spring Security's standard "I'm authenticated" object:
             * - principal: The user ID (who is this?)
             * - credentials: null (JWT already proves identity)
             * - authorities: The user's roles/permissions
             *
             * Collections.singletonList() with a SimpleGrantedAuthority
             * creates a role like "ROLE_USER" that Spring Security can
             * use for authorization checks (@PreAuthorize etc.)
             */
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userId,       // principal
                            null,          // credentials
                            Collections.singletonList(
                                    new org.springframework.security.core.authority
                                            .SimpleGrantedAuthority("ROLE_" + role)
                            )
                    );

            // Set request details (IP, session ID) for auditing
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            /*
             * Store the auth in SecurityContextHolder.
             * From this point on, the controller can access the
             * authenticated user via:
             *   SecurityContextHolder.getContext().getAuthentication()
             * or simply by injecting the @AuthenticationPrincipal annotation.
             */
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // Always continue the filter chain — even if no token,
        // the request might be to a public endpoint (like /login)
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT from the Authorization header.
     * Expected format: "Authorization: Bearer eyJhbGci..."
     *
     * @param request The incoming HTTP request
     * @return The JWT string, or null if not present
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(ServiceConstants.HEADER_AUTHORIZATION);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(ServiceConstants.TOKEN_PREFIX)) {
            // Strip "Bearer " (7 characters) to get the raw token
            return bearerToken.substring(7);
        }
        return null;
    }
}
