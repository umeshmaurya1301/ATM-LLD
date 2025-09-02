package com.umesh.atm.entity;


import com.umesh.atm.enums.AtmStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.infra.commons.entity.BaseEntity;

import java.time.Instant;

/**
 * Entity representing an ATM Location.
 * Inherits auditing fields and optimistic locking from BaseEntity.
 * Embeds an Address component for location details.
 */
@Entity
@Table(
        name = "atm_machine",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_atm_code", columnNames = {"atm_code"})
        }
)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AtmMachine extends BaseEntity {

    /** Primary key - auto-generated ID for ATM location */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    Long id;

    // Unique ATM code within bank network
    @Column(name = "atm_code", nullable = false, unique = true, length = 64)
    String atmCode;

    // IFSC code representing the managing branch
    @Column(name = "ifsc_code", nullable = false, length = 11)
    String ifscCode;

    // Current operational status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    AtmStatus status = AtmStatus.ACTIVE;

    // Transaction capabilities supported by the ATM
    @Column(name = "supports_deposit", nullable = false)
    boolean supportsDeposit;

    @Column(name = "supports_withdrawal", nullable = false)
    @Builder.Default
    boolean supportsWithdrawal = true;

    @Column(name = "supports_cardless", nullable = false)
    boolean supportsCardless;

    // Installation and maintenance dates
    @Column(name = "installation_date")
    Instant installationDate;

    @Column(name = "last_maintenance_date")
    Instant lastMaintenanceDate;

    // Cash capacity (max cash compressed in notes)
    @Column(name = "cash_capacity")
    Integer cashCapacity;

    // Contact number of managing branch/helpdesk etc.
    @Column(name = "branch_contact_number", length = 15)
    String branchContactNumber;

    // Software and hardware details
    @Column(name = "software_version", length = 50)
    String softwareVersion;

    @Column(name = "machine_model", length = 100)
    String machineModel;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_machine_location"))
    AtmLocation location;
}
