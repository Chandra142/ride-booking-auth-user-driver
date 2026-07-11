package com.ridebooking.driver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * ============================================================
 * REDIS CONFIGURATION
 * ============================================================
 *
 * Redis is an in-memory key-value store. It's MUCH faster than
 * a relational database because:
 * 1. Data is stored in RAM, not on disk
 * 2. No SQL parsing or query planning
 * 3. No table joins or ACID overhead
 *
 * We use Redis specifically for DRIVER LOCATIONS because:
 * - GPS coordinates change every 2-3 seconds
 * - We need FAST writes (every location update)
 * - We need FAST reads (finding nearby drivers)
 * - Location data is transient (current location matters,
 *   old locations are just history)
 *
 * If we used PostgreSQL for location updates:
 * - Every update = INSERT or UPDATE query
 * - Indexes need updating
 * - WAL (Write-Ahead Log) overhead
 * - Can cause lock contention at scale
 *
 * Redis KEY FORMAT:
 * We use "driver:location:{driverId}" as the key.
 * The colon-separated format is a Redis convention for namespacing.
 * Example: driver:location:DRV-001 → "12.9716,77.5946,1700000000"
 *
 * WHY StringRedisSerializer?
 * We don't need Java serialization here — we store simple strings
 * like "lat,lng,timestamp". String serialization is human-readable,
 * cross-platform, and uses less memory than Java serialization.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serialization for keys and values
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
