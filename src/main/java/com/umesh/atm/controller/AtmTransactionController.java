package com.umesh.atm.controller;

import com.umesh.atm.chain.HandlerResult;
import com.umesh.atm.chain.auth.AuthenticationChain;
import com.umesh.atm.chain.auth.AuthenticationRequest;
import com.umesh.atm.chain.transaction.TransactionProcessingChain;
import com.umesh.atm.chain.transaction.TransactionRequest;
import com.umesh.atm.entity.AtmMachine;
import com.umesh.atm.service.AtmSessionService;
import com.umesh.atm.service.BalanceService;
import com.umesh.atm.service.CashService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller demonstrating the use of Chain of Responsibility pattern
 * in ATM transaction processing.
 */
@RestController
@RequestMapping("/api/atm")
@RequiredArgsConstructor
@Slf4j
public class AtmTransactionController {
    
    private final AuthenticationChain authenticationChain;
    private final TransactionProcessingChain transactionProcessingChain;
    private final AtmSessionService sessionService;
    private final BalanceService balanceService;
    private final CashService cashService;
    
    /**
     * Authenticates user with PIN using authentication chain.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<Map<String, Object>> authenticate(@RequestBody AuthenticationRequestDto request) {
        log.info("Authentication request for card token: {}", request.getCardToken());
        
        try {
            // Get ATM machine from session or request
            AtmMachine atmMachine = sessionService.getAtmMachineFromSession(request.getSessionId());
            
            // Build authentication request
            AuthenticationRequest authRequest = AuthenticationRequest.builder()
                    .cardToken(request.getCardToken())
                    .pin(request.getPin())
                    .atmMachine(atmMachine)
                    .sessionId(request.getSessionId())
                    .clientIp(request.getClientIp())
                    .userAgent(request.getUserAgent())
                    .build();
            
            // Process through authentication chain
            HandlerResult result = authenticationChain.authenticate(authRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            
            if (!result.isSuccess()) {
                response.put("errorCode", result.getErrorCode());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create session after successful authentication
            String sessionId = sessionService.createSession(atmMachine, request.getCardToken());
            
            response.put("authenticated", true);
            response.put("sessionId", sessionId);
            response.put("sessionTimeoutSeconds", sessionService.getSessionTimeoutSeconds());
            response.put("message", "Authentication successful. Session created.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during authentication", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Authentication service error");
            errorResponse.put("errorCode", "AUTH_SERVICE_ERROR");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Processes cash withdrawal using transaction processing chain.
     */
    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(@RequestBody WithdrawalRequestDto request) {
        log.info("Withdrawal request for amount: {} from card: {}", 
                request.getAmount(), request.getCardToken());
        
        try {
            // Get ATM machine from session
            AtmMachine atmMachine = sessionService.getAtmMachineFromSession(request.getSessionId());
            
            // Build transaction request
            TransactionRequest txnRequest = TransactionRequest.builder()
                    .sessionId(request.getSessionId())
                    .cardToken(request.getCardToken())
                    .atmMachine(atmMachine)
                    .processingCode("010000") // Withdrawal processing code
                    .amount(request.getAmount())
                    .currency("INR")
                    .pin(request.getPin())
                    .build();
            
            // Process through transaction chain
            HandlerResult result = transactionProcessingChain.processTransaction(txnRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            
            if (!result.isSuccess()) {
                response.put("errorCode", result.getErrorCode());
                return ResponseEntity.badRequest().body(response);
            }
            
            // If validation passed, process the actual withdrawal
            boolean withdrawalSuccess = cashService.withdrawCash(atmMachine, request.getCardToken(), request.getAmount());
            
            response.put("withdrawalProcessed", withdrawalSuccess);
            response.put("amount", request.getAmount());
            response.put("denominationDistribution", 
                    txnRequest.getContextValue("denominationDistribution", Map.class));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during withdrawal processing", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Withdrawal service error");
            errorResponse.put("errorCode", "WITHDRAWAL_SERVICE_ERROR");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Processes balance inquiry using simplified transaction chain.
     */
    @PostMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@RequestBody BalanceInquiryRequestDto request) {
        log.info("Balance inquiry request from card: {}", request.getCardToken());
        
        try {
            // Get ATM machine from session
            AtmMachine atmMachine = sessionService.getAtmMachineFromSession(request.getSessionId());
            
            // Build transaction request for balance inquiry
            TransactionRequest txnRequest = TransactionRequest.builder()
                    .sessionId(request.getSessionId())
                    .cardToken(request.getCardToken())
                    .atmMachine(atmMachine)
                    .processingCode("310000") // Balance inquiry processing code
                    .amount(BigDecimal.ZERO)
                    .currency("INR")
                    .build();
            
            // Process through balance inquiry chain (simplified)
            HandlerResult result = transactionProcessingChain.processBalanceInquiry(txnRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            
            if (!result.isSuccess()) {
                response.put("errorCode", result.getErrorCode());
                return ResponseEntity.badRequest().body(response);
            }
            
            // If validation passed, get the actual balance
            BigDecimal currentBalance = balanceService.getCurrentBalance(request.getCardToken());
            BigDecimal availableBalance = balanceService.getAvailableBalance(request.getCardToken());
            BigDecimal dailyLimit = balanceService.getRemainingDailyLimit(request.getCardToken());
            
            response.put("currentBalance", currentBalance);
            response.put("availableBalance", availableBalance);
            response.put("remainingDailyLimit", dailyLimit);
            response.put("accountType", balanceService.getAccountType(request.getCardToken()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during balance inquiry", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Balance inquiry service error");
            errorResponse.put("errorCode", "BALANCE_SERVICE_ERROR");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Terminates user session (logout).
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody LogoutRequestDto request) {
        log.info("Logout request for session: {}", request.getSessionId());
        
        try {
            boolean terminated = sessionService.terminateSession(request.getSessionId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", terminated);
            response.put("message", terminated ? "Session terminated successfully" : "Session not found or already terminated");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during logout", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Logout service error");
            errorResponse.put("errorCode", "LOGOUT_SERVICE_ERROR");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // DTOs for request handling
    @Getter
    @Setter
    public static class AuthenticationRequestDto {
        private String cardToken;
        private String pin;
        private String sessionId;
        private String clientIp;
        private String userAgent;
    }

    @Getter
    @Setter
    public static class WithdrawalRequestDto {
        private String cardToken;
        private String sessionId;
        private BigDecimal amount;
        private String pin;
    }

    @Getter
    @Setter
    public static class BalanceInquiryRequestDto {
        private String cardToken;
        private String sessionId;
    }

    @Getter
    @Setter
    public static class LogoutRequestDto {
        private String sessionId;
    }
}
