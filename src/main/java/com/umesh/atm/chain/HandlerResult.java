package com.umesh.atm.chain;

import lombok.Builder;
import lombok.Getter;

/**
 * Result object for Chain of Responsibility handlers.
 * Encapsulates the outcome of processing and control flow decisions.
 */
@Getter
@Builder
public class HandlerResult {
    
    private final boolean success;
    private final boolean shouldContinue;
    private final String message;
    private final String errorCode;
    private final Object data;
    
    /**
     * Creates a successful result that continues to next handler.
     */
    public static HandlerResult success() {
        return HandlerResult.builder()
                .success(true)
                .shouldContinue(true)
                .build();
    }
    
    /**
     * Creates a successful result with data that continues to next handler.
     */
    public static HandlerResult success(String message, Object data) {
        return HandlerResult.builder()
                .success(true)
                .shouldContinue(true)
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * Creates a successful result that stops the chain.
     */
    public static HandlerResult successAndStop(String message) {
        return HandlerResult.builder()
                .success(true)
                .shouldContinue(false)
                .message(message)
                .build();
    }
    
    /**
     * Creates a failure result that stops the chain.
     */
    public static HandlerResult failure(String message) {
        return HandlerResult.builder()
                .success(false)
                .shouldContinue(false)
                .message(message)
                .build();
    }
    
    /**
     * Creates a failure result with error code that stops the chain.
     */
    public static HandlerResult failure(String message, String errorCode) {
        return HandlerResult.builder()
                .success(false)
                .shouldContinue(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
    
    /**
     * Creates a failure result that allows continuing to next handler.
     */
    public static HandlerResult failureButContinue(String message) {
        return HandlerResult.builder()
                .success(false)
                .shouldContinue(true)
                .message(message)
                .build();
    }
}
