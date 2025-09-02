package com.umesh.atm.chain.auth.handlers;

import com.umesh.atm.chain.Handler;
import com.umesh.atm.chain.HandlerResult;
import com.umesh.atm.chain.auth.AuthenticationRequest;
import com.umesh.atm.entity.Card;
import com.umesh.atm.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for card-level security validation in authentication chain.
 * Performs card status checks and security validations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CardSecurityHandler extends Handler<AuthenticationRequest> {
    
    private final CardService cardService;
    
    @Override
    protected HandlerResult doHandle(AuthenticationRequest request) {
        log.debug("Validating card security for card: {}", request.getCardToken());
        
        try {
            // Get card details
            Card card = cardService.getCardByToken(request.getCardToken());
            if (card == null) {
                log.warn("Card not found during security check: {}", request.getCardToken());
                return HandlerResult.failure("Card not found", "CARD_NOT_FOUND");
            }
            
            // Validate card is active and not blocked
            if (!cardService.isCardActive(card)) {
                log.warn("Card is not active during security check: {}", request.getCardToken());
                return HandlerResult.failure("Card is blocked or inactive", "CARD_INACTIVE");
            }
            
            // Check card expiry
            if (cardService.isCardExpired(card)) {
                log.warn("Card is expired during security check: {}", request.getCardToken());
                return HandlerResult.failure("Card has expired", "CARD_EXPIRED");
            }
            
            // Store card information in security context
            request.setSecurityContextValue("card", card);
            request.setSecurityContextValue("cardBrand", card.getBrand());
            request.setSecurityContextValue("cardIin", card.getIin());
            
            log.debug("Card security validation successful for card: {}", request.getCardToken());
            return HandlerResult.success("Card security validated", card);
            
        } catch (Exception e) {
            log.error("Error during card security validation for card: {}", request.getCardToken(), e);
            return HandlerResult.failure("Card security validation error", "CARD_SECURITY_ERROR");
        }
    }
}
