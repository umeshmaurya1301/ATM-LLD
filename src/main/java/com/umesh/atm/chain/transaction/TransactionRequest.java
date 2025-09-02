package com.umesh.atm.chain.transaction;

import com.umesh.atm.entity.AtmMachine;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Request object for transaction processing chain.
 * Contains all necessary information for transaction validation and processing.
 */
@Getter
@Setter
@Builder
public class TransactionRequest {
    
    private final String sessionId;
    private final String cardToken;
    private final AtmMachine atmMachine;
    private final String processingCode;
    private final BigDecimal amount;
    private final String currency;
    private final String pin;
    
    // Additional context data that handlers can use and modify
    @Builder.Default
    private final Map<String, Object> context = new HashMap<>();
    
    /**
     * Gets context value by key.
     */
    @SuppressWarnings("unchecked")
    public <T> T getContextValue(String key, Class<T> type) {
        Object value = context.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Sets context value.
     */
    public void setContextValue(String key, Object value) {
        context.put(key, value);
    }
    
    /**
     * Checks if this is a withdrawal transaction.
     */
    public boolean isWithdrawal() {
        return "01".equals(processingCode) || "010000".equals(processingCode);
    }
    
    /**
     * Checks if this is a balance inquiry transaction.
     */
    public boolean isBalanceInquiry() {
        return "31".equals(processingCode) || "310000".equals(processingCode);
    }
    
    /**
     * Checks if this is a deposit transaction.
     */
    public boolean isDeposit() {
        return "21".equals(processingCode) || "210000".equals(processingCode);
    }
}
