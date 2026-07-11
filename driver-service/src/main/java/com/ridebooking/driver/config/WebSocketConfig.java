package com.ridebooking.driver.config;

import com.ridebooking.driver.websocket.TrackingWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * ============================================================
 * WEBSOCKET CONFIGURATION
 * ============================================================
 *
 * WebSocket is a protocol that maintains a persistent, two-way
 * connection between client and server. Unlike HTTP (request-response),
 * WebSocket allows the server to PUSH data to the client at any time.
 *
 * USE CASE: Real-time driver tracking
 * - Driver's phone sends GPS every 2 seconds
 * - Server receives it and broadcasts to all connected clients
 *   who are watching that ride
 * - Rider's browser updates the map WITHOUT refreshing
 *
 * HOW HTTP vs WEBSOCKET:
 * HTTP:  Client → "Give me driver location" → Server responds → Connection closes
 *        Client → "Give me driver location" → Server responds → Connection closes
 *        (Repeated polling — wasteful)
 *
 * WebSocket: Client ↔ Server (persistent connection)
 *            Server → "Driver is at 12.9716, 77.5946"
 *            Server → "Driver is at 12.9717, 77.5948"
 *            (Server pushes updates as they happen)
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TrackingWebSocketHandler trackingHandler;

    public WebSocketConfig(TrackingWebSocketHandler trackingHandler) {
        this.trackingHandler = trackingHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        /*
         * Register the tracking handler at the /ws/tracking endpoint.
         * setAllowedOrigins("*") allows connections from any origin
         * (browser, mobile app, Postman WebSocket test).
         *
         * In production, restrict this to your actual frontend domains.
         */
        registry.addHandler(trackingHandler, "/ws/tracking")
                .setAllowedOrigins("*");
    }
}
