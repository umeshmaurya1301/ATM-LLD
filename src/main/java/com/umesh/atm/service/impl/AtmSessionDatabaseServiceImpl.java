package com.umesh.atm.service.impl;

import com.umesh.atm.entity.AtmMachine;
import com.umesh.atm.entity.AtmSession;
import com.umesh.atm.repository.AtmSessionRepository;
import com.umesh.atm.service.AtmSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Database-backed implementation of AtmSessionService.
 * Uses JPA entities for session persistence and management.
 */
@Service("databaseSessionService")
@RequiredArgsConstructor
@Slf4j
public class AtmSessionDatabaseServiceImpl implements AtmSessionService {

    @Value("${atm.session.timeout-seconds:300}")
    private int sessionTimeoutSeconds;

    private final AtmSessionRepository sessionRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String createSession(AtmMachine atmMachine, String cardToken) {
        log.info("Creating new database session for card: {} at ATM: {}", cardToken, atmMachine.getAtmCode());
        
        try {
            // Generate secure session token
            String sessionToken = generateSecureSessionToken();
            
            // Calculate expiry time
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(sessionTimeoutSeconds);
            
            // Create session entity
            AtmSession session = AtmSession.builder()
                    .sessionToken(sessionToken)
                    .cardToken(cardToken)
                    .atmMachine(atmMachine)
                    .status(AtmSession.SessionStatus.ACTIVE)
                    .sessionStartedAt(now)
                    .lastActivityAt(now)
                    .expiresAt(expiresAt)
                    .timeoutSeconds(sessionTimeoutSeconds)
                    .apiCallCount(0)
                    .build();
            
            // Save to database
            sessionRepository.save(session);
            
            log.info("Database session created successfully: {} for card: {}", sessionToken, cardToken);
            return sessionToken;
            
        } catch (Exception e) {
            log.error("Error creating database session for card: {}", cardToken, e);
            throw new RuntimeException("Failed to create session", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSessionValid(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }
        
        try {
            Optional<AtmSession> sessionOpt = sessionRepository.findActiveSessionByToken(sessionId, Instant.now());
            
            if (sessionOpt.isEmpty()) {
                log.debug("Session not found or expired: {}", sessionId);
                return false;
            }
            
            AtmSession session = sessionOpt.get();
            
            // Double-check session is still valid
            if (!session.isActive()) {
                log.debug("Session is not active: {}", sessionId);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error validating session: {}", sessionId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean extendSession(String sessionId) {
        try {
            Optional<AtmSession> sessionOpt = sessionRepository.findActiveSessionByToken(sessionId, Instant.now());
            
            if (sessionOpt.isEmpty()) {
                log.warn("Cannot extend non-existent or expired session: {}", sessionId);
                return false;
            }
            
            AtmSession session = sessionOpt.get();
            
            if (!session.isActive()) {
                log.warn("Cannot extend inactive session: {}", sessionId);
                return false;
            }
            
            // Update session activity
            Instant now = Instant.now();
            Instant newExpiresAt = now.plusSeconds(sessionTimeoutSeconds);
            
            int updated = sessionRepository.updateSessionActivity(sessionId, now, newExpiresAt);
            
            if (updated > 0) {
                log.debug("Session extended: {}", sessionId);
                return true;
            } else {
                log.warn("Failed to extend session: {}", sessionId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error extending session: {}", sessionId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean terminateSession(String sessionId) {
        try {
            Instant now = Instant.now();
            int updated = sessionRepository.terminateSession(sessionId, now, "User logout");
            
            if (updated > 0) {
                log.info("Session terminated: {}", sessionId);
                return true;
            } else {
                log.warn("Session not found or already terminated: {}", sessionId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error terminating session: {}", sessionId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getCardTokenFromSession(String sessionId) {
        try {
            Optional<AtmSession> sessionOpt = sessionRepository.findActiveSessionByToken(sessionId, Instant.now());
            return sessionOpt.map(AtmSession::getCardToken).orElse(null);
        } catch (Exception e) {
            log.error("Error getting card token from session: {}", sessionId, e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AtmMachine getAtmMachineFromSession(String sessionId) {
        try {
            Optional<AtmSession> sessionOpt = sessionRepository.findActiveSessionByToken(sessionId, Instant.now());
            return sessionOpt.map(AtmSession::getAtmMachine).orElse(null);
        } catch (Exception e) {
            log.error("Error getting ATM machine from session: {}", sessionId, e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Instant getSessionCreationTime(String sessionId) {
        try {
            Optional<AtmSession> sessionOpt = sessionRepository.findActiveSessionByToken(sessionId, Instant.now());
            return sessionOpt.map(AtmSession::getSessionStartedAt).orElse(null);
        } catch (Exception e) {
            log.error("Error getting session creation time: {}", sessionId, e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Instant getSessionLastActivity(String sessionId) {
        try {
            Optional<AtmSession> sessionOpt = sessionRepository.findActiveSessionByToken(sessionId, Instant.now());
            return sessionOpt.map(AtmSession::getLastActivityAt).orElse(null);
        } catch (Exception e) {
            log.error("Error getting session last activity: {}", sessionId, e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSessionTimedOut(String sessionId) {
        try {
            Optional<AtmSession> sessionOpt = sessionRepository.findActiveSessionByToken(sessionId, Instant.now());
            return sessionOpt.map(AtmSession::isExpired).orElse(true);
        } catch (Exception e) {
            log.error("Error checking session timeout: {}", sessionId, e);
            return true;
        }
    }

    @Override
    public int getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    @Override
    @Transactional
    public int cleanupExpiredSessions() {
        try {
            Instant now = Instant.now();
            
            // Mark expired sessions as expired
            int markedExpired = sessionRepository.markExpiredSessions(now);
            
            // Get count of expired sessions for logging
            List<AtmSession> expiredSessions = sessionRepository.findExpiredSessions(now);
            int expiredCount = expiredSessions.size();
            
            if (markedExpired > 0) {
                log.info("Marked {} expired sessions as expired", markedExpired);
            }
            
            return markedExpired;
            
        } catch (Exception e) {
            log.error("Error cleaning up expired sessions", e);
            return 0;
        }
    }

    @Override
    @Transactional
    public int terminateAllSessionsForCard(String cardToken) {
        try {
            Instant now = Instant.now();
            int terminated = sessionRepository.terminateAllSessionsForCard(cardToken, now, "Security termination");
            
            if (terminated > 0) {
                log.info("Terminated {} sessions for card: {}", terminated, cardToken);
            }
            
            return terminated;
            
        } catch (Exception e) {
            log.error("Error terminating sessions for card: {}", cardToken, e);
            return 0;
        }
    }

    /**
     * Generates a secure session token using cryptographically secure random bytes.
     */
    private String generateSecureSessionToken() {
        byte[] randomBytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Additional method to get session statistics (useful for monitoring).
     */
    @Transactional(readOnly = true)
    public long getActiveSessionCount() {
        try {
            return sessionRepository.count();
        } catch (Exception e) {
            log.error("Error getting active session count", e);
            return 0;
        }
    }

    /**
     * Additional method to get active sessions for a card (useful for security).
     */
    @Transactional(readOnly = true)
    public List<AtmSession> getActiveSessionsForCard(String cardToken) {
        try {
            return sessionRepository.findActiveSessionsByCardToken(cardToken, Instant.now());
        } catch (Exception e) {
            log.error("Error getting active sessions for card: {}", cardToken, e);
            return List.of();
        }
    }
}
