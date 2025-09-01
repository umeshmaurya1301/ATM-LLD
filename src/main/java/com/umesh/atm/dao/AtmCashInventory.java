package com.umesh.atm.dao;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.infra.commons.entity.BaseEntity;

@Entity
@Table(name = "atm_cash_inventory",
        uniqueConstraints = @UniqueConstraint(name = "uk_machine_denom", columnNames = {"machine_id","denomination"}))
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AtmCashInventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_inventory_machine"))
    AtmMachine machine;

    // Denomination value in currency minor units to avoid float issues (e.g., 10000 = ₹100.00)
    @Column(name = "denomination", nullable = false)
    Integer denomination;

    // Number of notes currently in the ATM for this denomination
    @Column(name = "note_count", nullable = false)
    Long noteCount;

    // Optional: track whether this denomination is currently dispensable
    @Column(name = "enabled", nullable = false)
    boolean enabled = true;
}
