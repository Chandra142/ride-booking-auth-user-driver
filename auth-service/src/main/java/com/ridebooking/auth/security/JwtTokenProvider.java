package com.ridebooking.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * ============================================================
 * JWT TOKEN PROVIDER
 * ============================================================
 *
 * This class handles ALL JWT operations:
 * - generateToken(): Creates a signed JWT when a user logs in
 * - validateToken(): Checks if a token is still valid
 * - getUserIdFromToken(): Extracts the user ID from a token
 *
 * WHAT IS A JWT?
 * JSON Web Token is a digitally signed JSON object. It looks like:
 *   eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozjgNryP4J3j1k
 *
 * It has three parts (separated by dots):
 * 1. HEADER: Algorithm type (HS256) and token type (JWT)
 * 2. PAYLOAD: Data (claims) — userId, email, role, expiration
 * 3. SIGNATURE: Cryptographic signature — verifies the token
 *    hasn't been tampered with
 *
 * WHY JWT?
 * - Stateless: The server doesn't need to store session data.
 *   The token itself contains everything needed to verify identity.
 * - Portable: Works across services — Auth Service signs it,
 *   User Service/Driver Service can verify it (or trust the Gateway).
 * - Standard: Every language has libraries for it.
 *
 * HOW SIGNING WORKS (HS256):
 * We use HMAC-SHA256 (a "symmetric" algorithm). The same secret
 * key is used to sign AND verify. Think of it like a wax seal:
 * only someone with the secret key can create the seal, but anyone
 * can check if the seal is intact.
 */
@Component
public class JwtTokenProvider {

    /*
     * The secret key comes from application.yml or environment variables.
     * In production, NEVER hardcode secrets — use environment variables
     * or a secret manager like Vault or AWS Secrets Manager.
     *
     * ${jwt.secret} reads from the "jwt.secret" property in application.yml.
     * The ":" after the key provides a default value if the property
     * is missing — but you MUST change this in production.
     */
    @Value("${jwt.secret:ThisIsMySecretKeyForRideBookingSystem2024!}")
    private String jwtSecret;

    /*
     * Token expiration in milliseconds.
     * Default: 15 minutes (900,000 ms).
     * Short-lived tokens limit damage if a token is stolen.
     */
    @Value("${jwt.expiration:900000}")
    private long jwtExpiration;

    /*
     * Refresh token expiration: 7 days (604,800,000 ms).
     * Long enough that users don't log in every day, short enough
     * that a stolen refresh token eventually becomes useless.
     */
    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenExpiration;

    /**
     * Converts the string secret into a Key object that JJWT can use.
     * Keys.hmacShaKeyFor() creates a valid HMAC-SHA key from the
     * secret bytes. It must be at least 256 bits (32 characters) long.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * ============================================================
     * GENERATE ACCESS TOKEN (short-lived)
     * ============================================================
     *
     * Called after successful login. Creates a JWT containing:
     * - subject (sub): The user's ID (the primary identifier)
     * - email: The user's email (custom claim)
     * - role: The user's role (custom claim)
     * - issuedAt (iat): When the token was created
     * - expiration (exp): When the token expires
     *
     * @param userId  The authenticated user's ID
     * @param email   The user's email
     * @param role    The user's role (USER, DRIVER, ADMIN)
     * @return        A signed JWT string
     */
    public String generateToken(String userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                // Header (algorithm) is auto-detected from the signing key
                .subject(userId)
                // ^^^ Standard claim "sub" — the user ID
                .claim("email", email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * ============================================================
     * GENERATE REFRESH TOKEN (long-lived)
     * ============================================================
     *
     * Similar to access token but with longer expiration.
     * Contains minimal claims — just the userId as subject.
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(userId)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * ============================================================
     * VALIDATE TOKEN
     * ============================================================
     *
     * Checks:
     * 1. The token's signature matches (it wasn't tampered with)
     * 2. The token hasn't expired
     * 3. The token's structure is valid
     *
     * If any check fails, an exception is thrown. We catch it
     * and return false — the caller handles the "invalid token" case.
     *
     * @param token The JWT string to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            /*
             * JWT exceptions cover:
             * - ExpiredJwtException: Token is past its expiration
             * - MalformedJwtException: Token is garbled
             * - SignatureException: Signature doesn't match
             * - UnsupportedJwtException: Wrong type of token
             */
            return false;
        }
    }

    /**
     * Extracts the user ID (subject claim) from a JWT.
     *
     * @param token The JWT string
     * @return The user ID stored in the "sub" claim
     */
    public String getUserIdFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Extracts the email claim from a JWT.
     */
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    /**
     * Extracts the role claim from a JWT.
     */
    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
}
