package com.umesh.atm.chain.transaction.handlers;

import com.umesh.atm.chain.Handler;
import com.umesh.atm.chain.HandlerResult;
import com.umesh.atm.chain.transaction.TransactionRequest;
import com.umesh.atm.service.AtmSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for validating ATM session in transaction processing chain.
 * Ensures the session is valid and active before proceeding with transaction.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionValidationHandler extends Handler<TransactionRequest> {
    
    private final AtmSessionService sessionService;
    
    @Override
    protected HandlerResult doHandle(TransactionRequest request) {
        log.debug("Validating session: {}", request.getSessionId());
        
        try {
            // Check if session exists and is valid
            if (!sessionService.isSessionValid(request.getSessionId())) {
                log.warn("Invalid or expired session: {}", request.getSessionId());
                return HandlerResult.failure("Session expired or invalid", "SESSION_INVALID");
            }
            
            // Verify session belongs to the card token
            String sessionCardToken = sessionService.getCardTokenFromSession(request.getSessionId());
            if (!request.getCardToken().equals(sessionCardToken)) {
                log.warn("Session card token mismatch. Expected: {}, Found: {}", 
                        request.getCardToken(), sessionCardToken);
                return HandlerResult.failure("Session validation failed", "SESSION_MISMATCH");
            }
            
            // Extend session due to activity
            sessionService.extendSession(request.getSessionId());
            
            log.debug("Session validation successful for session: {}", request.getSessionId());
            return HandlerResult.success("Session validated successfully", null);
            
        } catch (Exception e) {
            log.error("Error during session validation for session: {}", request.getSessionId(), e);
            return HandlerResult.failure("Session validation error", "SESSION_ERROR");
        }
    }
}
