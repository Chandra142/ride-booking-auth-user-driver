package com.ridebooking.driver.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ============================================================
 * TRACKING WEBSOCKET HANDLER
 * ============================================================
 *
 * This handler manages WebSocket connections for real-time
 * driver tracking. When a driver's location updates, the server
 * broadcasts it to all clients watching that ride.
 *
 * HOW IT WORKS:
 * 1. Client connects to ws://localhost:8083/ws/tracking?rideId=xxx
 * 2. We store the session with a key like "ride:xxx"
 * 3. When a driver updates their location, we find all sessions
 *    for that ride and send them the new coordinates
 * 4. When the client disconnects, we clean up the session
 *
 * ConcurrentHashMap: A thread-safe Map that allows multiple
 * threads to read/write simultaneously without locking the
 * entire map. Essential for a WebSocket server handling many
 * concurrent connections.
 */
@Component
public class TrackingWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(TrackingWebSocketHandler.class);

    /*
     * Stores active WebSocket sessions, grouped by ride ID.
     * Key: rideId (e.g., "RIDE-001")
     * Value: Map of sessionId → WebSocketSession
     *
     * We use nested ConcurrentHashMaps for thread safety.
     */
    private final Map<String, Map<String, WebSocketSession>> rideSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public TrackingWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Called when a new WebSocket connection is established.
     * We extract the rideId from the URL query parameter
     * and register this session under that ride.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Extract rideId from URL query: ?rideId=TEST-RIDE-001
        String query = session.getUri().getQuery();
        String rideId = parseQueryParam(query, "rideId");

        if (rideId == null) {
            rideId = "UNKNOWN";
        }

        // Register session under this ride
        rideSessions.computeIfAbsent(rideId, k -> new ConcurrentHashMap<>())
                .put(session.getId(), session);

        log.info("WebSocket connected: rideId={}, sessionId={}", rideId, session.getId());
    }

    /**
     * Called when the server receives a message from a client.
     * For our use case, this is where the driver's phone sends
     * location updates, and we broadcast them to all watchers.
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // The message is expected to be JSON with driverId, latitude, longitude
        // We broadcast it to all sessions watching the same ride
        String rideId = getRideIdForSession(session.getId());

        if (rideId != null) {
            broadcastToRide(rideId, message.getPayload());
        }
    }

    /**
     * Called when a WebSocket connection is closed.
     * We remove the session from our tracking map.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String rideId = getRideIdForSession(session.getId());

        if (rideId != null) {
            Map<String, WebSocketSession> sessions = rideSessions.get(rideId);
            if (sessions != null) {
                sessions.remove(session.getId());
                if (sessions.isEmpty()) {
                    rideSessions.remove(rideId);
                }
            }
        }

        log.info("WebSocket disconnected: sessionId={}", session.getId());
    }

    /**
     * Broadcasts a location update to all clients watching a specific ride.
     *
     * @param rideId The ride to broadcast to
     * @param message The JSON message containing location data
     */
    public void broadcastToRide(String rideId, String message) {
        Map<String, WebSocketSession> sessions = rideSessions.get(rideId);

        if (sessions != null) {
            for (WebSocketSession session : sessions.values()) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        log.error("Failed to send WebSocket message to session {}",
                                session.getId(), e);
                    }
                }
            }
        }
    }

    /**
     * Helper method to find which ride a session belongs to.
     */
    private String getRideIdForSession(String sessionId) {
        for (Map.Entry<String, Map<String, WebSocketSession>> entry : rideSessions.entrySet()) {
            if (entry.getValue().containsKey(sessionId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Simple query parameter parser.
     * In production, use a proper URI template matcher.
     */
    private String parseQueryParam(String query, String param) {
        if (query == null) return null;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && kv[0].equals(param)) {
                return kv[1];
            }
        }
        return null;
    }
}
