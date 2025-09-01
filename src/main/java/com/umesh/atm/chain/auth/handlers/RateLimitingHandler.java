package com.umesh.atm.chain.auth.handlers;

import com.umesh.atm.chain.Handler;
import com.umesh.atm.chain.HandlerResult;
import com.umesh.atm.chain.auth.AuthenticationRequest;
import com.umesh.atm.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for rate limiting authentication attempts.
 * Prevents brute force attacks by limiting authentication attempts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingHandler extends Handler<AuthenticationRequest> {
    
    private final AuthenticationService authenticationService;
    
    @Override
    protected HandlerResult doHandle(AuthenticationRequest request) {
        log.debug("Checking rate limiting for card: {}", request.getCardToken());
        
        try {
            // Check current failed attempt count
            int failedAttempts = authenticationService.getFailedAttempts(request.getCardToken());
            int maxAttempts = authenticationService.getMaxFailedAttempts();
            
            log.debug("Current failed attempts: {} / {} for card: {}", 
                    failedAttempts, maxAttempts, request.getCardToken());
            
            // Check if card should be blocked due to too many attempts
            if (authenticationService.shouldBlockCard(request.getCardToken())) {
                log.warn("Card should be blocked due to rate limiting: {}", request.getCardToken());
                return HandlerResult.failure(
                    String.format("Too many failed attempts. Card will be blocked after %d attempts.", maxAttempts),
                    "RATE_LIMIT_EXCEEDED"
                );
            }
            
            // Store attempt information in security context
            request.setSecurityContextValue("failedAttempts", failedAttempts);
            request.setSecurityContextValue("maxAttempts", maxAttempts);
            request.setSecurityContextValue("remainingAttempts", maxAttempts - failedAttempts);
            
            log.debug("Rate limiting check passed for card: {}", request.getCardToken());
            return HandlerResult.success("Rate limiting check passed", null);
            
        } catch (Exception e) {
            log.error("Error during rate limiting check for card: {}", request.getCardToken(), e);
            return HandlerResult.failure("Rate limiting check error", "RATE_LIMIT_ERROR");
        }
    }
}
