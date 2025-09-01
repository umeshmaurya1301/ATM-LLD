package com.umesh.atm.chain.auth;

import com.umesh.atm.dao.AtmMachine;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Request object for authentication processing chain.
 * Contains authentication credentials and security context.
 */
@Getter
@Setter
@Builder
public class AuthenticationRequest {
    
    private final String cardToken;
    private final String pin;
    private final AtmMachine atmMachine;
    private final String sessionId;
    private final String clientIp;
    private final String userAgent;
    
    // Security context that handlers can use and modify
    @Builder.Default
    private final Map<String, Object> securityContext = new HashMap<>();
    
    /**
     * Gets security context value by key.
     */
    @SuppressWarnings("unchecked")
    public <T> T getSecurityContextValue(String key, Class<T> type) {
        Object value = securityContext.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Sets security context value.
     */
    public void setSecurityContextValue(String key, Object value) {
        securityContext.put(key, value);
    }
}
