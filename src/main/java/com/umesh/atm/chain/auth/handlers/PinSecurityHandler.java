package com.umesh.atm.chain.auth.handlers;

import com.umesh.atm.chain.Handler;
import com.umesh.atm.chain.HandlerResult;
import com.umesh.atm.chain.auth.AuthenticationRequest;
import com.umesh.atm.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for PIN security validation in authentication chain.
 * Validates PIN format and performs authentication.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PinSecurityHandler extends Handler<AuthenticationRequest> {
    
    private final AuthenticationService authenticationService;
    
    @Override
    protected HandlerResult doHandle(AuthenticationRequest request) {
        log.debug("Validating PIN security for card: {}", request.getCardToken());
        
        try {
            // Validate PIN format first
            if (!authenticationService.isValidPinFormat(request.getPin())) {
                log.warn("Invalid PIN format for card: {}", request.getCardToken());
                return HandlerResult.failure("Invalid PIN format", "PIN_INVALID_FORMAT");
            }
            
            // Perform PIN authentication
            boolean isAuthenticated = authenticationService.authenticatePin(
                    request.getCardToken(), request.getPin());
            
            if (!isAuthenticated) {
                log.warn("PIN authentication failed for card: {}", request.getCardToken());
                
                // Increment failed attempts (this will be handled by rate limiting)
                int failedAttempts = authenticationService.incrementFailedAttempts(request.getCardToken());
                
                return HandlerResult.failure(
                    String.format("Incorrect PIN. Failed attempts: %d", failedAttempts),
                    "PIN_INCORRECT"
                );
            }
            
            // Reset failed attempts on successful authentication
            authenticationService.resetFailedAttempts(request.getCardToken());
            
            // Store authentication success in security context
            request.setSecurityContextValue("pinAuthenticated", true);
            request.setSecurityContextValue("authenticationTimestamp", System.currentTimeMillis());
            
            log.info("PIN authentication successful for card: {}", request.getCardToken());
            return HandlerResult.success("PIN authentication successful", null);
            
        } catch (Exception e) {
            log.error("Error during PIN security validation for card: {}", request.getCardToken(), e);
            return HandlerResult.failure("PIN security validation error", "PIN_SECURITY_ERROR");
        }
    }
}
