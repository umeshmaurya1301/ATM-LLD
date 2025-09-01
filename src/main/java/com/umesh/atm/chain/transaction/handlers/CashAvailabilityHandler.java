package com.umesh.atm.chain.transaction.handlers;

import com.umesh.atm.chain.Handler;
import com.umesh.atm.chain.HandlerResult;
import com.umesh.atm.chain.transaction.TransactionRequest;
import com.umesh.atm.service.CashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Handler for validating cash availability in ATM for withdrawal transactions.
 * Checks if ATM has sufficient cash and can dispense the requested amount.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CashAvailabilityHandler extends Handler<TransactionRequest> {
    
    private final CashService cashService;
    
    @Override
    protected HandlerResult doHandle(TransactionRequest request) {
        log.debug("Validating cash availability for transaction");
        
        try {
            // Only check cash availability for withdrawal transactions
            if (!request.isWithdrawal()) {
                log.debug("Non-withdrawal transaction, skipping cash availability check");
                return HandlerResult.success("Cash availability check skipped", null);
            }
            
            BigDecimal amount = request.getAmount();
            
            // Validate withdrawal amount format
            if (!cashService.isValidWithdrawalAmount(amount)) {
                log.warn("Invalid withdrawal amount: {}", amount);
                return HandlerResult.failure("Invalid withdrawal amount", "INVALID_WITHDRAWAL_AMOUNT");
            }
            
            // Check if ATM has sufficient cash
            if (!cashService.hasSufficientCash(request.getAtmMachine(), amount)) {
                log.warn("ATM has insufficient cash for amount: {} at ATM: {}", 
                        amount, request.getAtmMachine().getAtmCode());
                return HandlerResult.failure("ATM has insufficient cash", "INSUFFICIENT_CASH_IN_ATM");
            }
            
            // Calculate denomination distribution
            Map<Integer, Integer> denominationDistribution = 
                    cashService.calculateDenominationDistribution(request.getAtmMachine(), amount);
            
            if (denominationDistribution == null || denominationDistribution.isEmpty()) {
                log.warn("Cannot dispense amount {} with available denominations at ATM: {}", 
                        amount, request.getAtmMachine().getAtmCode());
                return HandlerResult.failure("Cannot dispense requested amount with available denominations", 
                        "CANNOT_DISPENSE_AMOUNT");
            }
            
            // Store denomination distribution in context for later use
            request.setContextValue("denominationDistribution", denominationDistribution);
            
            log.debug("Cash availability validation successful for amount: {} at ATM: {}", 
                    amount, request.getAtmMachine().getAtmCode());
            return HandlerResult.success("Cash availability validated", denominationDistribution);
            
        } catch (Exception e) {
            log.error("Error during cash availability validation", e);
            return HandlerResult.failure("Cash availability validation error", "CASH_AVAILABILITY_ERROR");
        }
    }
}
