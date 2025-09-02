package com.umesh.atm.service;

import com.umesh.atm.entity.AtmMachine;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Service interface for cash-related operations in ATM system.
 * Handles cash withdrawal, deposit, and inventory management.
 */
public interface CashService {
    
    /**
     * Processes cash withdrawal request.
     * 
     * @param atmMachine the ATM machine processing the request
     * @param cardToken the tokenized card identifier
     * @param amount the amount to withdraw
     * @return true if withdrawal was successful
     */
    boolean withdrawCash(AtmMachine atmMachine, String cardToken, BigDecimal amount);
    
    /**
     * Processes cash deposit request.
     * 
     * @param atmMachine the ATM machine processing the request
     * @param cardToken the tokenized card identifier
     * @param amount the amount to deposit
     * @return true if deposit was successful
     */
    boolean depositCash(AtmMachine atmMachine, String cardToken, BigDecimal amount);
    
    /**
     * Checks if ATM has sufficient cash for withdrawal.
     * 
     * @param atmMachine the ATM machine to check
     * @param amount the amount to check availability for
     * @return true if sufficient cash is available
     */
    boolean hasSufficientCash(AtmMachine atmMachine, BigDecimal amount);
    
    /**
     * Gets available cash denominations and their counts.
     * 
     * @param atmMachine the ATM machine to check
     * @return map of denomination to available count
     */
    Map<Integer, Long> getAvailableDenominations(AtmMachine atmMachine);
    
    /**
     * Calculates optimal denomination distribution for withdrawal.
     * 
     * @param atmMachine the ATM machine
     * @param amount the amount to withdraw
     * @return map of denomination to count to dispense, null if not possible
     */
    Map<Integer, Integer> calculateDenominationDistribution(AtmMachine atmMachine, BigDecimal amount);
    
    /**
     * Updates cash inventory after transaction.
     * 
     * @param atmMachine the ATM machine
     * @param denominationChanges map of denomination to count change (negative for withdrawal)
     * @return true if inventory was updated successfully
     */
    boolean updateCashInventory(AtmMachine atmMachine, Map<Integer, Integer> denominationChanges);
    
    /**
     * Validates withdrawal amount against business rules.
     * 
     * @param amount the amount to validate
     * @return true if amount is valid for withdrawal
     */
    boolean isValidWithdrawalAmount(BigDecimal amount);
    
    /**
     * Validates deposit amount against business rules.
     * 
     * @param amount the amount to validate
     * @return true if amount is valid for deposit
     */
    boolean isValidDepositAmount(BigDecimal amount);
    
    /**
     * Gets total available cash in the ATM.
     * 
     * @param atmMachine the ATM machine to check
     * @return total cash amount available
     */
    BigDecimal getTotalAvailableCash(AtmMachine atmMachine);
}
