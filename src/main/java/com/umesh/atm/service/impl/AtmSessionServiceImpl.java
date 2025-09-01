package com.umesh.atm.service.impl;

import com.umesh.atm.dao.AtmMachine;
import com.umesh.atm.service.AtmSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of AtmSessionService for session management.
 * Handles session lifecycle, timeout management, and security.
 */
@Service
@Slf4j
public class AtmSessionServiceImpl implements AtmSessionService {

    @Value("${atm.session.timeout-seconds:300}")
    private int sessionTimeoutSeconds;

    // In-memory session storage (in production, use Redis or database)
    private final Map<String, SessionData> sessionStorage = new ConcurrentHashMap<>();
    
    // Secure random for session ID generation
    private final SecureRandom secureRandom = new SecureRandom();
    
    // Scheduled executor for session cleanup
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    public AtmSessionServiceImpl() {
        // Schedule periodic cleanup of expired sessions
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredSessions, 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public String createSession(AtmMachine atmMachine, String cardToken) {
        log.info("Creating new session for card: {} at ATM: {}", cardToken, atmMachine.getAtmCode());
        
        try {
            // Generate secure session ID
            String sessionId = generateSecureSessionId();
            
            // Create session data
            SessionData sessionData = new SessionData(
                sessionId,
                cardToken,
                atmMachine,
                Instant.now(),
                Instant.now(),
                false // not terminated
            );
            
            // Store session
            sessionStorage.put(sessionId, sessionData);
            
            log.info("Session created successfully: {} for card: {}", sessionId, cardToken);
            return sessionId;
            
        } catch (Exception e) {
            log.error("Error creating session for card: {}", cardToken, e);
            throw new RuntimeException("Failed to create session", e);
        }
    }

    @Override
    public boolean isSessionValid(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }
        
        SessionData sessionData = sessionStorage.get(sessionId);
        if (sessionData == null) {
            log.debug("Session not found: {}", sessionId);
            return false;
        }
        
        if (sessionData.isTerminated()) {
            log.debug("Session terminated: {}", sessionId);
            return false;
        }
        
        if (hasSessionTimedOut(sessionId)) {
            log.debug("Session timed out: {}", sessionId);
            // Remove expired session
            sessionStorage.remove(sessionId);
            return false;
        }
        
        return true;
    }

    @Override
    public boolean extendSession(String sessionId) {
        SessionData sessionData = sessionStorage.get(sessionId);
        if (sessionData == null) {
            log.warn("Cannot extend non-existent session: {}", sessionId);
            return false;
        }
        
        if (sessionData.isTerminated()) {
            log.warn("Cannot extend terminated session: {}", sessionId);
            return false;
        }
        
        // Update last activity time
        sessionData.setLastActivity(Instant.now());
        log.debug("Session extended: {}", sessionId);
        return true;
    }

    @Override
    public boolean terminateSession(String sessionId) {
        SessionData sessionData = sessionStorage.get(sessionId);
        if (sessionData == null) {
            log.warn("Cannot terminate non-existent session: {}", sessionId);
            return false;
        }
        
        sessionData.setTerminated(true);
        sessionStorage.remove(sessionId);
        log.info("Session terminated: {}", sessionId);
        return true;
    }

    @Override
    public String getCardTokenFromSession(String sessionId) {
        SessionData sessionData = sessionStorage.get(sessionId);
        return sessionData != null ? sessionData.getCardToken() : null;
    }

    @Override
    public AtmMachine getAtmMachineFromSession(String sessionId) {
        SessionData sessionData = sessionStorage.get(sessionId);
        return sessionData != null ? sessionData.getAtmMachine() : null;
    }

    @Override
    public Instant getSessionCreationTime(String sessionId) {
        SessionData sessionData = sessionStorage.get(sessionId);
        return sessionData != null ? sessionData.getCreationTime() : null;
    }

    @Override
    public Instant getSessionLastActivity(String sessionId) {
        SessionData sessionData = sessionStorage.get(sessionId);
        return sessionData != null ? sessionData.getLastActivity() : null;
    }

    @Override
    public boolean hasSessionTimedOut(String sessionId) {
        SessionData sessionData = sessionStorage.get(sessionId);
        if (sessionData == null) {
            return true;
        }
        
        Instant now = Instant.now();
        Instant timeoutThreshold = sessionData.getLastActivity().plusSeconds(sessionTimeoutSeconds);
        return now.isAfter(timeoutThreshold);
    }

    @Override
    public int getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    @Override
    public int cleanupExpiredSessions() {
        int cleanedCount = 0;
        Instant now = Instant.now();
        
        sessionStorage.entrySet().removeIf(entry -> {
            SessionData sessionData = entry.getValue();
            Instant timeoutThreshold = sessionData.getLastActivity().plusSeconds(sessionTimeoutSeconds);
            return now.isAfter(timeoutThreshold) || sessionData.isTerminated();
        });
        
        cleanedCount = sessionStorage.size();
        if (cleanedCount > 0) {
            log.info("Cleaned up {} expired sessions", cleanedCount);
        }
        
        return cleanedCount;
    }

    @Override
    public int terminateAllSessionsForCard(String cardToken) {
        int terminatedCount = 0;
        
        sessionStorage.entrySet().removeIf(entry -> {
            SessionData sessionData = entry.getValue();
            return cardToken.equals(sessionData.getCardToken());
        });
        
        terminatedCount = sessionStorage.size();
        if (terminatedCount > 0) {
            log.info("Terminated {} sessions for card: {}", terminatedCount, cardToken);
        }
        
        return terminatedCount;
    }

    /**
     * Generates a secure session ID using cryptographically secure random bytes.
     */
    private String generateSecureSessionId() {
        byte[] randomBytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Internal class to store session data.
     */
    private static class SessionData {
        private final String sessionId;
        private final String cardToken;
        private final AtmMachine atmMachine;
        private final Instant creationTime;
        private Instant lastActivity;
        private boolean terminated;

        public SessionData(String sessionId, String cardToken, AtmMachine atmMachine, 
                          Instant creationTime, Instant lastActivity, boolean terminated) {
            this.sessionId = sessionId;
            this.cardToken = cardToken;
            this.atmMachine = atmMachine;
            this.creationTime = creationTime;
            this.lastActivity = lastActivity;
            this.terminated = terminated;
        }

        // Getters and setters
        public String getSessionId() { return sessionId; }
        public String getCardToken() { return cardToken; }
        public AtmMachine getAtmMachine() { return atmMachine; }
        public Instant getCreationTime() { return creationTime; }
        public Instant getLastActivity() { return lastActivity; }
        public void setLastActivity(Instant lastActivity) { this.lastActivity = lastActivity; }
        public boolean isTerminated() { return terminated; }
        public void setTerminated(boolean terminated) { this.terminated = terminated; }
    }
}