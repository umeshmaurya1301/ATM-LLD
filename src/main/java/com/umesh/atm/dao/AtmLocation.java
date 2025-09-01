package com.umesh.atm.dao;

import com.umesh.atm.enums.AtmStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import com.umesh.atm.entity.Address;
import com.umesh.atm.entity.BaseEntity;

import java.math.BigDecimal;


/**
 * Entity representing an ATM Location.
 * Inherits auditing fields and optimistic locking from BaseEntity.
 * Embeds an Address component for location details.
 */
@Entity
@Table(
        name = "atm_location",
        indexes = {
                @Index(name = "idx_city", columnList = "city"),
                @Index(name = "idx_pin_code", columnList = "pin_code")
        }
)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AtmLocation extends BaseEntity {

    /** Primary key - auto-generated ID for ATM location */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    Long id;

    /** Embedded address fields for ATM location */
    @Embedded
    @AttributeOverride(name = "line1", column = @Column(name = "line1", nullable = false))
    @AttributeOverride(name = "line2", column = @Column(name = "line2"))
    @AttributeOverride(name = "line3", column = @Column(name = "line3"))
    @AttributeOverride(name = "landmark", column = @Column(name = "landmark"))
    @AttributeOverride(name = "city", column = @Column(name = "city", nullable = false, length = 120))
    @AttributeOverride(name = "district", column = @Column(name = "district", length = 120))
    @AttributeOverride(name = "state", column = @Column(name = "state", length = 120))
    @AttributeOverride(name = "country", column = @Column(name = "country", length = 120))
    @AttributeOverride(name = "pinCode", column = @Column(name = "pin_code", length = 10))
    Address address;

    /** Optional geographic coordinates for location-based queries */
    @Column(name = "latitude", precision = 10, scale = 7)
    BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    BigDecimal longitude;

    /** Current operational status of the ATM */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    AtmStatus status = AtmStatus.ACTIVE;

    /** Flags indicating supported transaction capabilities */
    @Column(name = "supports_deposit", nullable = false)
    boolean supportsDeposit;

    @Column(name = "supports_withdrawal", nullable = false)
    @Builder.Default
    boolean supportsWithdrawal = true;

    @Column(name = "supports_cardless", nullable = false)
    boolean supportsCardless;

    // Additional fields (branch, operating hours, cash capacities) can be added here as needed.

}
