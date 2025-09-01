package com.umesh.atm.service;

import java.math.BigDecimal;

/**
 * Service interface for balance inquiry operations in ATM system.
 * Handles account balance retrieval and validation.
 */
public interface BalanceService {
    
    /**
     * Retrieves current account balance for the card.
     * 
     * @param cardToken the tokenized card identifier
     * @return current account balance, null if unable to retrieve
     */
    BigDecimal getCurrentBalance(String cardToken);
    
    /**
     * Retrieves available balance (considering holds and limits).
     * 
     * @param cardToken the tokenized card identifier
     * @return available balance for transactions
     */
    BigDecimal getAvailableBalance(String cardToken);
    
    /**
     * Checks if account has sufficient balance for transaction.
     * 
     * @param cardToken the tokenized card identifier
     * @param amount the transaction amount to check
     * @return true if sufficient balance is available
     */
    boolean hasSufficientBalance(String cardToken, BigDecimal amount);
    
    /**
     * Gets daily withdrawal limit for the card.
     * 
     * @param cardToken the tokenized card identifier
     * @return daily withdrawal limit
     */
    BigDecimal getDailyWithdrawalLimit(String cardToken);
    
    /**
     * Gets remaining daily withdrawal limit.
     * 
     * @param cardToken the tokenized card identifier
     * @return remaining withdrawal limit for today
     */
    BigDecimal getRemainingDailyLimit(String cardToken);
    
    /**
     * Checks if transaction amount is within daily limits.
     * 
     * @param cardToken the tokenized card identifier
     * @param amount the transaction amount
     * @return true if within daily limits
     */
    boolean isWithinDailyLimit(String cardToken, BigDecimal amount);
    
    /**
     * Gets account type information.
     * 
     * @param cardToken the tokenized card identifier
     * @return account type (e.g., SAVINGS, CURRENT, CREDIT)
     */
    String getAccountType(String cardToken);
    
    /**
     * Validates if balance inquiry is allowed for the account.
     * 
     * @param cardToken the tokenized card identifier
     * @return true if balance inquiry is permitted
     */
    boolean isBalanceInquiryAllowed(String cardToken);
}
