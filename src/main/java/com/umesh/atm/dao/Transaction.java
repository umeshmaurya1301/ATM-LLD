package com.umesh.atm.dao;


import com.umesh.atm.enums.TxnStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.umesh.atm.entity.BaseEntity;

import java.time.Instant;

@Entity
@Table(
        name = "atm_transaction",
        indexes = {
                @Index(name = "idx_txn_rrn", columnList = "rrn"),
                @Index(name = "idx_txn_terminal", columnList = "terminal_id"),
                @Index(name = "idx_txn_time", columnList = "local_txn_datetime")
        }
)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Core ISO 8583 identifiers (request/response correlation)
    @Column(name = "mti", nullable = false, length = 4)
    String mti; // Message Type Indicator (e.g., 0200/0210). [1]

    @Column(name = "processing_code", nullable = false, length = 6)
    String processingCode; // DE 3 - operation type (e.g., cash withdrawal). [1]

    @Column(name = "stan", nullable = false, length = 6)
    String stan; // DE 11 - System Trace Audit Number. [1]

    @Column(name = "rrn", nullable = false, length = 12)
    String rrn; // DE 37 - Retrieval Reference Number. [1]

    @Column(name = "response_code", length = 2)
    String responseCode; // DE 39 - outcome (may be null until response). [1]

    // Terminal and routing
    @Column(name = "terminal_id", nullable = false, length = 16)
    String terminalId; // DE 41. [1]

    @Column(name = "network_id", length = 3)
    String networkId; // DE 24 (NII), optional but useful. [1]

    // Amount and currency (minor units for safety)
    @Column(name = "amount_minor", nullable = false)
    Long amountMinor; // DE 4 - transaction amount in minor units. [1]

    @Column(name = "currency", nullable = false, length = 3)
    String currency; // DE 49 - ISO 4217 (numeric or alpha per profile). [1]

    // Timing
    @Column(name = "local_txn_datetime", nullable = false)
    Instant localTxnDateTime; // Combine DE 12/13 into an Instant (UTC). [1]

    // Derived status for app logic
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    TxnStatus status = TxnStatus.PENDING; // APPROVED/DECLINED set post-response.
}
