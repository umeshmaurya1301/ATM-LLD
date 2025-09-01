package com.umesh.atm.service;

/**
 * Service interface for authentication operations in ATM system.
 * Handles PIN verification and authentication security.
 */
public interface AuthenticationService {
    
    /**
     * Authenticates user with PIN for the given card.
     * 
     * @param cardToken the tokenized card identifier
     * @param pin the PIN entered by user
     * @return true if PIN is correct and authentication successful
     */
    boolean authenticatePin(String cardToken, String pin);
    
    /**
     * Validates PIN format and basic security rules.
     * 
     * @param pin the PIN to validate
     * @return true if PIN format is valid
     */
    boolean isValidPinFormat(String pin);
    
    /**
     * Tracks failed authentication attempts for a card.
     * 
     * @param cardToken the tokenized card identifier
     * @return current count of failed attempts
     */
    int getFailedAttempts(String cardToken);
    
    /**
     * Increments failed attempt counter for a card.
     * 
     * @param cardToken the tokenized card identifier
     * @return updated count of failed attempts
     */
    int incrementFailedAttempts(String cardToken);
    
    /**
     * Resets failed attempt counter after successful authentication.
     * 
     * @param cardToken the tokenized card identifier
     */
    void resetFailedAttempts(String cardToken);
    
    /**
     * Checks if card should be blocked due to too many failed attempts.
     * 
     * @param cardToken the tokenized card identifier
     * @return true if card should be blocked
     */
    boolean shouldBlockCard(String cardToken);
    
    /**
     * Gets maximum allowed failed attempts before blocking card.
     * 
     * @return maximum failed attempts threshold
     */
    int getMaxFailedAttempts();
}
