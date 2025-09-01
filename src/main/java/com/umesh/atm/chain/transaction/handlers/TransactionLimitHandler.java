package com.umesh.atm.chain.transaction.handlers;

import com.umesh.atm.chain.Handler;
import com.umesh.atm.chain.HandlerResult;
import com.umesh.atm.chain.transaction.TransactionRequest;
import com.umesh.atm.service.BalanceService;
import com.umesh.atm.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Handler for validating transaction limits in transaction processing chain.
 * Checks daily limits, transaction counts, and amount validations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionLimitHandler extends Handler<TransactionRequest> {
    
    private final BalanceService balanceService;
    private final TransactionService transactionService;
    
    @Override
    protected HandlerResult doHandle(TransactionRequest request) {
        log.debug("Validating transaction limits for card: {}", request.getCardToken());
        
        try {
            // Check daily transaction count limit
            if (transactionService.isDailyTransactionLimitExceeded(request.getCardToken())) {
                log.warn("Daily transaction limit exceeded for card: {}", request.getCardToken());
                return HandlerResult.failure("Daily transaction limit exceeded", "DAILY_TXN_LIMIT_EXCEEDED");
            }
            
            // For withdrawal transactions, check additional limits
            if (request.isWithdrawal()) {
                return validateWithdrawalLimits(request);
            }
            
            // For balance inquiry, no amount limits to check
            if (request.isBalanceInquiry()) {
                if (!balanceService.isBalanceInquiryAllowed(request.getCardToken())) {
                    log.warn("Balance inquiry not allowed for card: {}", request.getCardToken());
                    return HandlerResult.failure("Balance inquiry not allowed", "BALANCE_INQUIRY_NOT_ALLOWED");
                }
            }
            
            log.debug("Transaction limits validation successful for card: {}", request.getCardToken());
            return HandlerResult.success("Transaction limits validated", null);
            
        } catch (Exception e) {
            log.error("Error during transaction limits validation for card: {}", request.getCardToken(), e);
            return HandlerResult.failure("Transaction limits validation error", "LIMITS_ERROR");
        }
    }
    
    private HandlerResult validateWithdrawalLimits(TransactionRequest request) {
        BigDecimal amount = request.getAmount();
        String cardToken = request.getCardToken();
        
        // Check if amount is within daily withdrawal limits
        if (!balanceService.isWithinDailyLimit(cardToken, amount)) {
            log.warn("Withdrawal amount {} exceeds daily limit for card: {}", amount, cardToken);
            BigDecimal remainingLimit = balanceService.getRemainingDailyLimit(cardToken);
            return HandlerResult.failure(
                String.format("Withdrawal amount exceeds daily limit. Remaining limit: %s", remainingLimit), 
                "DAILY_WITHDRAWAL_LIMIT_EXCEEDED"
            );
        }
        
        // Check if account has sufficient balance
        if (!balanceService.hasSufficientBalance(cardToken, amount)) {
            log.warn("Insufficient balance for withdrawal amount {} for card: {}", amount, cardToken);
            return HandlerResult.failure("Insufficient account balance", "INSUFFICIENT_BALANCE");
        }
        
        // Store remaining daily limit in context
        BigDecimal remainingLimit = balanceService.getRemainingDailyLimit(cardToken);
        request.setContextValue("remainingDailyLimit", remainingLimit);
        
        return HandlerResult.success("Withdrawal limits validated", null);
    }
}
