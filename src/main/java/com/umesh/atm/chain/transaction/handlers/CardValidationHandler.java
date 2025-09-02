package com.umesh.atm.chain.transaction.handlers;

import com.umesh.atm.chain.Handler;
import com.umesh.atm.chain.HandlerResult;
import com.umesh.atm.chain.transaction.TransactionRequest;
import com.umesh.atm.entity.Card;
import com.umesh.atm.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for validating card in transaction processing chain.
 * Ensures the card is valid, active, and not expired.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CardValidationHandler extends Handler<TransactionRequest> {
    
    private final CardService cardService;
    
    @Override
    protected HandlerResult doHandle(TransactionRequest request) {
        log.debug("Validating card: {}", request.getCardToken());
        
        try {
            // Get card details
            Card card = cardService.getCardByToken(request.getCardToken());
            if (card == null) {
                log.warn("Card not found: {}", request.getCardToken());
                return HandlerResult.failure("Card not found", "CARD_NOT_FOUND");
            }
            
            // Check if card is active
            if (!cardService.isCardActive(card)) {
                log.warn("Card is not active: {}", request.getCardToken());
                return HandlerResult.failure("Card is blocked or inactive", "CARD_INACTIVE");
            }
            
            // Check if card is expired
            if (cardService.isCardExpired(card)) {
                log.warn("Card is expired: {}", request.getCardToken());
                return HandlerResult.failure("Card has expired", "CARD_EXPIRED");
            }
            
            // Store card in context for later use
            request.setContextValue("card", card);
            
            log.debug("Card validation successful for card: {}", request.getCardToken());
            return HandlerResult.success("Card validated successfully", card);
            
        } catch (Exception e) {
            log.error("Error during card validation for card: {}", request.getCardToken(), e);
            return HandlerResult.failure("Card validation error", "CARD_ERROR");
        }
    }
}
