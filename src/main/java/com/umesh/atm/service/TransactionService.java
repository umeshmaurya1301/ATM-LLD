package com.umesh.atm.service;

import com.umesh.atm.dao.AtmMachine;
import com.umesh.atm.dao.Transaction;
import com.umesh.atm.enums.TxnStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Service interface for transaction management in ATM system.
 * Handles transaction logging, processing, and history.
 */
public interface TransactionService {
    
    /**
     * Creates a new transaction record.
     * 
     * @param atmMachine the ATM machine processing the transaction
     * @param cardToken the tokenized card identifier
     * @param processingCode the transaction type code
     * @param amount the transaction amount
     * @return created Transaction entity
     */
    Transaction createTransaction(AtmMachine atmMachine, String cardToken, String processingCode, BigDecimal amount);
    
    /**
     * Updates transaction status and response details.
     * 
     * @param transactionId the transaction ID
     * @param status the new transaction status
     * @param responseCode the response code from processing
     * @return updated Transaction entity
     */
    Transaction updateTransactionStatus(Long transactionId, TxnStatus status, String responseCode);
    
    /**
     * Logs transaction completion with final details.
     * 
     * @param transactionId the transaction ID
     * @param success whether transaction was successful
     * @param responseCode the final response code
     * @return true if logging was successful
     */
    boolean logTransactionCompletion(Long transactionId, boolean success, String responseCode);
    
    /**
     * Retrieves transaction history for a card.
     * 
     * @param cardToken the tokenized card identifier
     * @param limit maximum number of transactions to return
     * @return list of recent transactions
     */
    List<Transaction> getTransactionHistory(String cardToken, int limit);
    
    /**
     * Retrieves transaction by ID.
     * 
     * @param transactionId the transaction ID
     * @return Transaction entity if found, null otherwise
     */
    Transaction getTransactionById(Long transactionId);
    
    /**
     * Retrieves transaction by RRN (Retrieval Reference Number).
     * 
     * @param rrn the retrieval reference number
     * @return Transaction entity if found, null otherwise
     */
    Transaction getTransactionByRrn(String rrn);
    
    /**
     * Generates unique RRN for transaction.
     * 
     * @return unique retrieval reference number
     */
    String generateRrn();
    
    /**
     * Generates unique STAN (System Trace Audit Number).
     * 
     * @return unique system trace audit number
     */
    String generateStan();
    
    /**
     * Validates transaction request parameters.
     * 
     * @param processingCode the transaction type code
     * @param amount the transaction amount
     * @return true if transaction parameters are valid
     */
    boolean validateTransactionRequest(String processingCode, BigDecimal amount);
    
    /**
     * Gets daily transaction count for a card.
     * 
     * @param cardToken the tokenized card identifier
     * @param date the date to check (null for today)
     * @return number of transactions for the date
     */
    int getDailyTransactionCount(String cardToken, Instant date);
    
    /**
     * Checks if daily transaction limit is exceeded.
     * 
     * @param cardToken the tokenized card identifier
     * @return true if daily limit is exceeded
     */
    boolean isDailyTransactionLimitExceeded(String cardToken);
    
    /**
     * Reverses a completed transaction.
     * 
     * @param originalTransactionId the ID of transaction to reverse
     * @param reason the reason for reversal
     * @return reversal Transaction entity
     */
    Transaction reverseTransaction(Long originalTransactionId, String reason);
}
