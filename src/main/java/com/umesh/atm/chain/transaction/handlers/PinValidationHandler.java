package com.umesh.atm.chain.transaction.handlers;

import com.umesh.atm.chain.Handler;
import com.umesh.atm.chain.HandlerResult;
import com.umesh.atm.chain.transaction.TransactionRequest;
import com.umesh.atm.service.AuthenticationService;
import com.umesh.atm.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for validating PIN in transaction processing chain.
 * Handles PIN authentication and failed attempt tracking.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PinValidationHandler extends Handler<TransactionRequest> {
    
    private final AuthenticationService authenticationService;
    private final CardService cardService;
    
    @Override
    protected HandlerResult doHandle(TransactionRequest request) {
        log.debug("Validating PIN for card: {}", request.getCardToken());
        
        try {
            // Skip PIN validation for balance inquiry if already authenticated in session
            if (request.isBalanceInquiry() && isPinAlreadyValidated(request)) {
                log.debug("PIN already validated in session, skipping validation");
                return HandlerResult.success("PIN validation skipped - already authenticated", null);
            }
            
            // Validate PIN format
            if (!authenticationService.isValidPinFormat(request.getPin())) {
                log.warn("Invalid PIN format for card: {}", request.getCardToken());
                return HandlerResult.failure("Invalid PIN format", "PIN_INVALID_FORMAT");
            }
            
            // Check if card should be blocked due to too many failed attempts
            if (authenticationService.shouldBlockCard(request.getCardToken())) {
                log.warn("Card should be blocked due to too many failed attempts: {}", request.getCardToken());
                cardService.blockCard(request.getCardToken());
                return HandlerResult.failure("Card blocked due to multiple failed attempts", "CARD_BLOCKED");
            }
            
            // Authenticate PIN
            if (!authenticationService.authenticatePin(request.getCardToken(), request.getPin())) {
                log.warn("PIN authentication failed for card: {}", request.getCardToken());
                authenticationService.incrementFailedAttempts(request.getCardToken());
                return HandlerResult.failure("Incorrect PIN", "PIN_INCORRECT");
            }
            
            // Reset failed attempts on successful authentication
            authenticationService.resetFailedAttempts(request.getCardToken());
            
            // Mark PIN as validated in context
            request.setContextValue("pinValidated", true);
            
            log.debug("PIN validation successful for card: {}", request.getCardToken());
            return HandlerResult.success("PIN validated successfully", null);
            
        } catch (Exception e) {
            log.error("Error during PIN validation for card: {}", request.getCardToken(), e);
            return HandlerResult.failure("PIN validation error", "PIN_ERROR");
        }
    }
    
    private boolean isPinAlreadyValidated(TransactionRequest request) {
        Boolean validated = request.getContextValue("pinValidated", Boolean.class);
        return Boolean.TRUE.equals(validated);
    }
}
