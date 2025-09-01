package com.umesh.atm.dao;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import com.umesh.atm.entity.BaseEntity;

@Entity
@Table(
        name = "atm_operator",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_operator_code", columnNames = {"operator_code"})
        },
        indexes = {
                @Index(name = "idx_operator_active", columnList = "active"),
                @Index(name = "idx_operator_role", columnList = "role")
        }
)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AtmOperator extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    // Unique identifier for the operator (internal or vendor-provided)
    @Column(name = "operator_code", nullable = false, length = 64, unique = true)
    String operatorCode;

    @Column(name = "name", nullable = false, length = 120)
    String name;

    // Optional contact details
    @Column(name = "phone", length = 20)
    String phone;

    @Column(name = "email", length = 191)
    String email;

    // Role classification (e.g., TECHNICIAN, CIT, SUPERVISOR); use enum or string
    @Column(name = "role", length = 32)
    String role;

    // Whether the operator is currently active/authorized
    @Column(name = "active", nullable = false)
    @Builder.Default
    boolean active = true;
}
