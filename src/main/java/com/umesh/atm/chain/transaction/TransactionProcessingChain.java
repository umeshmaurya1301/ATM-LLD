package com.umesh.atm.chain.transaction;

import com.umesh.atm.chain.Handler;
import com.umesh.atm.chain.HandlerResult;
import com.umesh.atm.chain.transaction.handlers.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Transaction processing chain coordinator.
 * Sets up and executes the chain of responsibility for transaction validation and processing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionProcessingChain {
    
    private final SessionValidationHandler sessionValidationHandler;
    private final CardValidationHandler cardValidationHandler;
    private final PinValidationHandler pinValidationHandler;
    private final TransactionLimitHandler transactionLimitHandler;
    private final CashAvailabilityHandler cashAvailabilityHandler;
    
    /**
     * Processes a transaction request through the validation chain.
     * 
     * @param request the transaction request to process
     * @return the result of chain processing
     */
    public HandlerResult processTransaction(TransactionRequest request) {
        log.info("Starting transaction processing chain for card: {} at ATM: {}", 
                request.getCardToken(), request.getAtmMachine().getAtmCode());
        
        try {
            // Build the chain of responsibility
            Handler<TransactionRequest> chain = buildChain();
            
            // Execute the chain
            HandlerResult result = chain.handle(request);
            
            if (result.isSuccess()) {
                log.info("Transaction validation chain completed successfully for card: {}", 
                        request.getCardToken());
            } else {
                log.warn("Transaction validation chain failed for card: {}. Error: {}", 
                        request.getCardToken(), result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Error in transaction processing chain for card: {}", request.getCardToken(), e);
            return HandlerResult.failure("Transaction processing chain error", "CHAIN_ERROR");
        }
    }
    
    /**
     * Builds the transaction processing chain.
     * The order of handlers is important for proper validation flow.
     */
    private Handler<TransactionRequest> buildChain() {
        // Set up the chain: Session -> Card -> PIN -> Limits -> Cash Availability
        sessionValidationHandler
                .setNext(cardValidationHandler)
                .setNext(pinValidationHandler)
                .setNext(transactionLimitHandler)
                .setNext(cashAvailabilityHandler);
        
        return sessionValidationHandler;
    }
    
    /**
     * Builds a simplified chain for balance inquiry (skips cash availability).
     */
    public HandlerResult processBalanceInquiry(TransactionRequest request) {
        log.info("Starting balance inquiry processing chain for card: {}", request.getCardToken());
        
        try {
            // Build simplified chain for balance inquiry
            Handler<TransactionRequest> chain = buildBalanceInquiryChain();
            
            HandlerResult result = chain.handle(request);
            
            if (result.isSuccess()) {
                log.info("Balance inquiry validation completed successfully for card: {}", 
                        request.getCardToken());
            } else {
                log.warn("Balance inquiry validation failed for card: {}. Error: {}", 
                        request.getCardToken(), result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Error in balance inquiry processing chain for card: {}", request.getCardToken(), e);
            return HandlerResult.failure("Balance inquiry processing error", "BALANCE_INQUIRY_ERROR");
        }
    }
    
    private Handler<TransactionRequest> buildBalanceInquiryChain() {
        // Simplified chain for balance inquiry: Session -> Card -> Limits (no PIN or cash check)
        sessionValidationHandler
                .setNext(cardValidationHandler)
                .setNext(transactionLimitHandler);
        
        return sessionValidationHandler;
    }
}
