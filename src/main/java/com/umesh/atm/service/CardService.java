package com.umesh.atm.service;

import com.umesh.atm.dao.Card;
import com.umesh.atm.enums.CardStatus;

/**
 * Service interface for card-related operations in ATM system.
 * Handles card insertion, validation, and status management.
 */
public interface CardService {
    
    /**
     * Validates if a card can be inserted and processed.
     * 
     * @param cardToken the tokenized card identifier
     * @return true if card is valid and can be processed
     */
    boolean validateCard(String cardToken);
    
    /**
     * Retrieves card information by token.
     * 
     * @param cardToken the tokenized card identifier
     * @return Card entity if found, null otherwise
     */
    Card getCardByToken(String cardToken);
    
    /**
     * Checks if card is expired based on current date.
     * 
     * @param card the card to check
     * @return true if card is expired
     */
    boolean isCardExpired(Card card);
    
    /**
     * Checks if card status allows transactions.
     * 
     * @param card the card to check
     * @return true if card is active and not blocked
     */
    boolean isCardActive(Card card);
    
    /**
     * Blocks a card due to security reasons (e.g., multiple failed PIN attempts).
     * 
     * @param cardToken the tokenized card identifier
     * @return true if card was successfully blocked
     */
    boolean blockCard(String cardToken);
    
    /**
     * Updates card status.
     * 
     * @param cardToken the tokenized card identifier
     * @param status the new status to set
     * @return true if status was updated successfully
     */
    boolean updateCardStatus(String cardToken, CardStatus status);
}
