package com.umesh.atm.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.infra.commons.entity.BaseEntity;

import java.time.Instant;

/**
 * Entity representing an ATM user session.
 * Tracks user authentication sessions for security, audit, and session management.
 */
@Entity
@Table(
        name = "atm_session",
        indexes = {
                @Index(name = "idx_session_token", columnList = "session_token", unique = true),
                @Index(name = "idx_session_card", columnList = "card_token"),
                @Index(name = "idx_session_atm", columnList = "atm_machine_id"),
                @Index(name = "idx_session_status", columnList = "status"),
                @Index(name = "idx_session_created", columnList = "created_at"),
                @Index(name = "idx_session_last_activity", columnList = "last_activity_at")
        }
)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AtmSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    /** Unique session token (cryptographically secure) */
    @Column(name = "session_token", nullable = false, unique = true, length = 128)
    String sessionToken;

    /** Associated card token (encrypted card identifier) */
    @Column(name = "card_token", nullable = false, length = 128)
    String cardToken;

    /** ATM machine where session was created */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atm_machine_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_session_atm_machine"))
    AtmMachine atmMachine;

    /** Session status */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    SessionStatus status = SessionStatus.ACTIVE;

    /** When the session was created */
    @Column(name = "session_started_at", nullable = false)
    Instant sessionStartedAt;

    /** Last activity timestamp (updated on each API call) */
    @Column(name = "last_activity_at", nullable = false)
    Instant lastActivityAt;

    /** When the session expires (calculated based on timeout) */
    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;

    /** When the session was terminated (if applicable) */
    @Column(name = "terminated_at")
    Instant terminatedAt;

    /** Reason for session termination */
    @Column(name = "termination_reason", length = 100)
    String terminationReason;

    /** Client IP address */
    @Column(name = "client_ip", length = 45) // IPv6 support
    String clientIp;

    /** User agent string */
    @Column(name = "user_agent", length = 500)
    String userAgent;

    /** Number of API calls made in this session */
    @Column(name = "api_call_count", nullable = false)
    @Builder.Default
    Integer apiCallCount = 0;

    /** Session timeout in seconds */
    @Column(name = "timeout_seconds", nullable = false)
    Integer timeoutSeconds;

    /** Additional session metadata (JSON format) */
    @Column(name = "metadata", columnDefinition = "TEXT")
    String metadata; // JSON string for additional session data

    /**
     * Checks if the session is currently active and not expired.
     */
    public boolean isActive() {
        return status == SessionStatus.ACTIVE && 
               Instant.now().isBefore(expiresAt) &&
               terminatedAt == null;
    }

    /**
     * Checks if the session has expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Extends the session by updating last activity and expiry time.
     */
    public void extendSession() {
        this.lastActivityAt = Instant.now();
        this.expiresAt = lastActivityAt.plusSeconds(timeoutSeconds);
        this.apiCallCount++;
    }

    /**
     * Terminates the session with a reason.
     */
    public void terminate(String reason) {
        this.status = SessionStatus.TERMINATED;
        this.terminatedAt = Instant.now();
        this.terminationReason = reason;
    }

    /**
     * Session status enumeration.
     */
    public enum SessionStatus {
        ACTIVE,     // Session is active and valid
        EXPIRED,    // Session has expired due to timeout
        TERMINATED, // Session was manually terminated
        INVALID     // Session is invalid due to security issues
    }
}
