package com.umesh.atm.chain;

/**
 * Abstract base class for Chain of Responsibility pattern.
 * Defines the interface for handling requests and chaining handlers.
 * 
 * @param <T> the type of request being processed
 */
public abstract class Handler<T> {
    
    private Handler<T> nextHandler;
    
    /**
     * Sets the next handler in the chain.
     * 
     * @param nextHandler the next handler to process the request
     * @return the next handler for method chaining
     */
    public Handler<T> setNext(Handler<T> nextHandler) {
        this.nextHandler = nextHandler;
        return nextHandler;
    }
    
    /**
     * Handles the request. If this handler cannot process it,
     * passes it to the next handler in the chain.
     * 
     * @param request the request to process
     * @return the result of processing
     */
    public HandlerResult handle(T request) {
        HandlerResult result = doHandle(request);
        
        if (result.isShouldContinue() && nextHandler != null) {
            HandlerResult nextResult = nextHandler.handle(request);
            // Merge results if needed
            return result.isSuccess() ? nextResult : result;
        }
        
        return result;
    }
    
    /**
     * Template method for specific handler implementation.
     * 
     * @param request the request to process
     * @return the result of processing
     */
    protected abstract HandlerResult doHandle(T request);
}
