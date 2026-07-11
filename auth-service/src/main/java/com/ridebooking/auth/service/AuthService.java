package com.ridebooking.auth.service;

import com.ridebooking.auth.dto.*;
import com.ridebooking.auth.entity.UserCredential;
import com.ridebooking.auth.repository.UserCredentialRepository;
import com.ridebooking.auth.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * ============================================================
 * AUTH SERVICE — BUSINESS LOGIC LAYER
 * ============================================================
 *
 * This is where all the real work happens. The Controller receives
 * raw HTTP requests and passes the data here. The Service:
 * 1. Validates business rules (is email already taken?)
 * 2. Coordinates between components (repository + JWT provider)
 * 3. Throws exceptions if something goes wrong
 *
 * WHY A SEPARATE SERVICE LAYER?
 * - Controllers should be thin — they only handle HTTP concerns
 *   (parse request, return response with proper status code)
 * - Business logic in controllers makes the code untestable
 *   (you'd need to make HTTP calls to test it)
 * - Services can be unit-tested independently
 * - If the API changes from REST to gRPC, the service layer
 *   doesn't change — only the controller does
 */
@Service
public class AuthService {

    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /*
     * Constructor injection — Spring automatically provides
     * these dependencies because they're all @Component/@Bean.
     *
     * WHY CONSTRUCTOR INJECTION (not @Autowired on fields)?
     * - The object is fully initialized when created (no null fields)
     * - Makes testing easy (pass mocks in the constructor)
     * - The class is immutable (fields can be final)
     */
    public AuthService(UserCredentialRepository userCredentialRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userCredentialRepository = userCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * ============================================================
     * REGISTER A NEW USER
     * ============================================================
     *
     * Flow:
     * 1. Check if email already exists → throw exception if yes
     * 2. Hash the password with BCrypt
     * 3. Create a UserCredential entity with the hashed password
     * 4. Save to database
     *
     * @param request Contains email, password, fullName
     * @return AuthResponse with tokens and user info
     */
    public AuthResponse register(RegisterRequest request) {

        // Step 1: Check for duplicate email
        if (userCredentialRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Step 2: Hash the password (never store raw passwords!)
        String hashedPassword = passwordEncoder.encode(request.password());

        // Step 3: Create and populate the entity
        UserCredential user = new UserCredential();
        user.setEmail(request.email().toLowerCase().trim());
        // ^^^ Always normalize emails to lowercase to avoid
        //     "John@Example.com" vs "john@example.com" duplicates
        user.setPassword(hashedPassword);
        user.setFullName(request.fullName().trim());
        // Default role is "USER" (set in the entity)

        // Step 4: Save to database
        UserCredential savedUser = userCredentialRepository.save(user);

        // Step 5: Generate JWT tokens
        String token = jwtTokenProvider.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getId());

        // Step 6: Return response (never include the password hash!)
        return new AuthResponse(
                token,
                refreshToken,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName()
        );
    }

    /**
     * ============================================================
     * LOGIN
     * ============================================================
     *
     * Flow:
     * 1. Find user by email
     * 2. Verify password matches the BCrypt hash
     * 3. If valid → generate JWT tokens
     * 4. If invalid → throw exception
     *
     * @param request Contains email and password
     * @return AuthResponse with tokens and user info
     * @throws BadCredentialsException if email/password don't match
     */
    public AuthResponse login(LoginRequest request) {

        // Step 1: Find the user
        UserCredential user = userCredentialRepository
                .findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        /*
         * Step 2: Verify password
         *
         * passwordEncoder.matches() takes the raw password and the
         * stored BCrypt hash, then:
         * 1. Extracts the salt from the stored hash
         * 2. Re-hashes the raw password with that salt
         * 3. Compares the results
         *
         * This is the ONLY safe way to check passwords. Never do:
         *   storedHash.equals(myHashFunction(rawPassword))  ← WRONG!
         * BCrypt hashes include the salt, so comparing raw strings
         * doesn't work.
         */
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Step 3: Generate tokens
        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // Step 4: Return response
        return new AuthResponse(
                token,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getFullName()
        );
    }

    /**
     * ============================================================
     * REFRESH TOKEN
     * ============================================================
     *
     * When the access token expires, the client can use the refresh
     * token to get a new access token without asking the user to
     * log in again.
     *
     * Flow:
     * 1. Validate the refresh token
     * 2. Extract the user ID from it
     * 3. Fetch the user from the database
     * 4. Issue a new access token + new refresh token (rotation)
     *
     * TOKEN ROTATION: We issue a NEW refresh token each time.
     * If an old refresh token is stolen, it becomes useless after
     * the legitimate user refreshes — the attacker's token is now
     * stale. This is a security best practice.
     *
     * @param request Contains the refresh token
     * @return New set of tokens
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        // Step 1: Validate the refresh token
        if (!jwtTokenProvider.validateToken(request.refreshToken())) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        // Step 2: Extract user ID
        String userId = jwtTokenProvider.getUserIdFromToken(request.refreshToken());

        // Step 3: Fetch user from database
        UserCredential user = userCredentialRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Step 4: Generate new tokens
        String newToken = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return new AuthResponse(
                newToken,
                newRefreshToken,
                user.getId(),
                user.getEmail(),
                user.getFullName()
        );
    }
}
