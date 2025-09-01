package com.umesh.atm.repository;

import com.umesh.atm.dao.AtmSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ATM session management.
 * Provides database operations for session persistence and queries.
 */
@Repository
public interface AtmSessionRepository extends JpaRepository<AtmSession, Long> {

    /**
     * Finds an active session by session token.
     */
    @Query("SELECT s FROM AtmSession s WHERE s.sessionToken = :sessionToken AND s.status = 'ACTIVE' AND s.expiresAt > :now")
    Optional<AtmSession> findActiveSessionByToken(@Param("sessionToken") String sessionToken, @Param("now") Instant now);

    /**
     * Finds all active sessions for a specific card token.
     */
    @Query("SELECT s FROM AtmSession s WHERE s.cardToken = :cardToken AND s.status = 'ACTIVE' AND s.expiresAt > :now")
    List<AtmSession> findActiveSessionsByCardToken(@Param("cardToken") String cardToken, @Param("now") Instant now);

    /**
     * Finds all active sessions for a specific ATM machine.
     */
    @Query("SELECT s FROM AtmSession s WHERE s.atmMachine.id = :atmMachineId AND s.status = 'ACTIVE' AND s.expiresAt > :now")
    List<AtmSession> findActiveSessionsByAtmMachine(@Param("atmMachineId") Long atmMachineId, @Param("now") Instant now);

    /**
     * Finds all expired sessions that need cleanup.
     */
    @Query("SELECT s FROM AtmSession s WHERE s.expiresAt <= :now AND s.status = 'ACTIVE'")
    List<AtmSession> findExpiredSessions(@Param("now") Instant now);

    /**
     * Terminates all active sessions for a specific card token.
     */
    @Modifying
    @Query("UPDATE AtmSession s SET s.status = 'TERMINATED', s.terminatedAt = :terminatedAt, s.terminationReason = :reason WHERE s.cardToken = :cardToken AND s.status = 'ACTIVE'")
    int terminateAllSessionsForCard(@Param("cardToken") String cardToken, @Param("terminatedAt") Instant terminatedAt, @Param("reason") String reason);

    /**
     * Terminates a specific session.
     */
    @Modifying
    @Query("UPDATE AtmSession s SET s.status = 'TERMINATED', s.terminatedAt = :terminatedAt, s.terminationReason = :reason WHERE s.sessionToken = :sessionToken")
    int terminateSession(@Param("sessionToken") String sessionToken, @Param("terminatedAt") Instant terminatedAt, @Param("reason") String reason);

    /**
     * Updates last activity and extends session expiry.
     */
    @Modifying
    @Query("UPDATE AtmSession s SET s.lastActivityAt = :lastActivity, s.expiresAt = :expiresAt, s.apiCallCount = s.apiCallCount + 1 WHERE s.sessionToken = :sessionToken")
    int updateSessionActivity(@Param("sessionToken") String sessionToken, @Param("lastActivity") Instant lastActivity, @Param("expiresAt") Instant expiresAt);

    /**
     * Marks expired sessions as expired.
     */
    @Modifying
    @Query("UPDATE AtmSession s SET s.status = 'EXPIRED' WHERE s.expiresAt <= :now AND s.status = 'ACTIVE'")
    int markExpiredSessions(@Param("now") Instant now);

    /**
     * Counts active sessions for a specific card token.
     */
    @Query("SELECT COUNT(s) FROM AtmSession s WHERE s.cardToken = :cardToken AND s.status = 'ACTIVE' AND s.expiresAt > :now")
    long countActiveSessionsByCardToken(@Param("cardToken") String cardToken, @Param("now") Instant now);

    /**
     * Counts active sessions for a specific ATM machine.
     */
    @Query("SELECT COUNT(s) FROM AtmSession s WHERE s.atmMachine.id = :atmMachineId AND s.status = 'ACTIVE' AND s.expiresAt > :now")
    long countActiveSessionsByAtmMachine(@Param("atmMachineId") Long atmMachineId, @Param("now") Instant now);

    /**
     * Finds sessions created within a time range for analytics.
     */
    @Query("SELECT s FROM AtmSession s WHERE s.sessionStartedAt BETWEEN :startTime AND :endTime ORDER BY s.sessionStartedAt DESC")
    List<AtmSession> findSessionsByTimeRange(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
}
