package com.umesh.atm.service;

import com.umesh.atm.dao.AtmMachine;

import java.time.Instant;

/**
 * Service interface for ATM session management.
 * Handles user sessions, timeouts, and session security.
 */
public interface AtmSessionService {
    
    /**
     * Creates a new ATM session for a card.
     * 
     * @param atmMachine the ATM machine
     * @param cardToken the tokenized card identifier
     * @return unique session ID
     */
    String createSession(AtmMachine atmMachine, String cardToken);
    
    /**
     * Validates if session is active and not expired.
     * 
     * @param sessionId the session identifier
     * @return true if session is valid and active
     */
    boolean isSessionValid(String sessionId);
    
    /**
     * Extends session timeout due to user activity.
     * 
     * @param sessionId the session identifier
     * @return true if session was extended successfully
     */
    boolean extendSession(String sessionId);
    
    /**
     * Terminates an active session.
     * 
     * @param sessionId the session identifier
     * @return true if session was terminated successfully
     */
    boolean terminateSession(String sessionId);
    
    /**
     * Gets card token associated with session.
     * 
     * @param sessionId the session identifier
     * @return card token if session exists, null otherwise
     */
    String getCardTokenFromSession(String sessionId);
    
    /**
     * Gets ATM machine associated with session.
     * 
     * @param sessionId the session identifier
     * @return ATM machine if session exists, null otherwise
     */
    AtmMachine getAtmMachineFromSession(String sessionId);
    
    /**
     * Gets session creation time.
     * 
     * @param sessionId the session identifier
     * @return session creation timestamp
     */
    Instant getSessionCreationTime(String sessionId);
    
    /**
     * Gets session last activity time.
     * 
     * @param sessionId the session identifier
     * @return last activity timestamp
     */
    Instant getSessionLastActivity(String sessionId);
    
    /**
     * Checks if session has timed out.
     * 
     * @param sessionId the session identifier
     * @return true if session has timed out
     */
    boolean hasSessionTimedOut(String sessionId);
    
    /**
     * Gets session timeout duration in seconds.
     * 
     * @return session timeout duration
     */
    int getSessionTimeoutSeconds();
    
    /**
     * Cleans up expired sessions.
     * 
     * @return number of sessions cleaned up
     */
    int cleanupExpiredSessions();
    
    /**
     * Forces termination of all sessions for a card (security measure).
     * 
     * @param cardToken the tokenized card identifier
     * @return number of sessions terminated
     */
    int terminateAllSessionsForCard(String cardToken);
}
