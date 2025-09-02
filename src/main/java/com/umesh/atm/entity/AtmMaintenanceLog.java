package com.umesh.atm.entity;

import java.time.Instant;

import com.umesh.atm.enums.IncidentSeverity;
import com.umesh.atm.enums.MaintenanceOutcome;
import com.umesh.atm.enums.MaintenanceType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.infra.commons.entity.BaseEntity;

@Entity
@Table(
        name = "atm_maintenance_log",
        indexes = {
                @Index(name = "idx_log_machine", columnList = "machine_id"),
                @Index(name = "idx_log_operator", columnList = "operator_id"),
                @Index(name = "idx_log_type", columnList = "type"),
                @Index(name = "idx_log_time", columnList = "event_time")
        }
)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AtmMaintenanceLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_log_machine"))
    AtmMachine machine;

    // The operator who performed or logged the activity (could be null for auto-generated events)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id",
            foreignKey = @ForeignKey(name = "fk_log_operator"))
    AtmOperator operator;

    // When the event actually happened (not just when recorded)
    @Column(name = "event_time", nullable = false)
    Instant eventTime;

    // Type of maintenance event
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    MaintenanceType type;

    // Outcome status
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 32)
    MaintenanceOutcome outcome = MaintenanceOutcome.SUCCESS;

    // Optional severity for incidents
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 32)
    IncidentSeverity severity;

    // Free-form description/notes; store enough for operational context
    @Lob
    @Column(name = "notes")
    String notes;

    // Optional machine telemetry or error codes
    @Column(name = "error_code")
    String errorCode;

    // Optional parts replaced (comma-separated or model a child table if you need structure)
    @Column(name = "parts_replaced")
    String partsReplaced;

    // Optional quantitative details for refills/cleanups; capture deltas to support reconciliation
    @Column(name = "refilled_total_notes")
    Long refilledTotalNotes;

    @Column(name = "removed_total_notes")
    Long removedTotalNotes;
}
