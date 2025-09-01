package com.umesh.atm.chain.auth;

import com.umesh.atm.chain.Handler;
import com.umesh.atm.chain.HandlerResult;
import com.umesh.atm.chain.auth.handlers.CardSecurityHandler;
import com.umesh.atm.chain.auth.handlers.PinSecurityHandler;
import com.umesh.atm.chain.auth.handlers.RateLimitingHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Authentication chain coordinator.
 * Sets up and executes the chain of responsibility for multi-step authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationChain {
    
    private final CardSecurityHandler cardSecurityHandler;
    private final RateLimitingHandler rateLimitingHandler;
    private final PinSecurityHandler pinSecurityHandler;
    
    /**
     * Processes an authentication request through the security validation chain.
     * 
     * @param request the authentication request to process
     * @return the result of chain processing
     */
    public HandlerResult authenticate(AuthenticationRequest request) {
        log.info("Starting authentication chain for card: {} at ATM: {}", 
                request.getCardToken(), request.getAtmMachine().getAtmCode());
        
        try {
            // Build the authentication chain
            Handler<AuthenticationRequest> chain = buildAuthenticationChain();
            
            // Execute the chain
            HandlerResult result = chain.handle(request);
            
            if (result.isSuccess()) {
                log.info("Authentication chain completed successfully for card: {}", 
                        request.getCardToken());
            } else {
                log.warn("Authentication chain failed for card: {}. Error: {}", 
                        request.getCardToken(), result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Error in authentication chain for card: {}", request.getCardToken(), e);
            return HandlerResult.failure("Authentication chain error", "AUTH_CHAIN_ERROR");
        }
    }
    
    /**
     * Builds the authentication processing chain.
     * Order: Card Security -> Rate Limiting -> PIN Security
     */
    private Handler<AuthenticationRequest> buildAuthenticationChain() {
        cardSecurityHandler
                .setNext(rateLimitingHandler)
                .setNext(pinSecurityHandler);
        
        return cardSecurityHandler;
    }
    
    /**
     * Processes a quick authentication check (without PIN) for certain operations.
     * Used for operations that might not require full PIN authentication.
     */
    public HandlerResult quickAuthenticate(AuthenticationRequest request) {
        log.info("Starting quick authentication chain for card: {}", request.getCardToken());
        
        try {
            // Build simplified chain without PIN validation
            Handler<AuthenticationRequest> chain = buildQuickAuthChain();
            
            HandlerResult result = chain.handle(request);
            
            if (result.isSuccess()) {
                log.info("Quick authentication completed successfully for card: {}", 
                        request.getCardToken());
            } else {
                log.warn("Quick authentication failed for card: {}. Error: {}", 
                        request.getCardToken(), result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Error in quick authentication chain for card: {}", request.getCardToken(), e);
            return HandlerResult.failure("Quick authentication error", "QUICK_AUTH_ERROR");
        }
    }
    
    private Handler<AuthenticationRequest> buildQuickAuthChain() {
        // Simplified chain: Card Security -> Rate Limiting (no PIN)
        cardSecurityHandler.setNext(rateLimitingHandler);
        return cardSecurityHandler;
    }
}
