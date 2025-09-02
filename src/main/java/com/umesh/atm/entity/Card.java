package com.umesh.atm.entity;


import com.umesh.atm.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;
import org.infra.commons.entity.BaseEntity;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "card",
        uniqueConstraints = @UniqueConstraint(name="uk_token", columnNames = "token"))
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Card extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Surrogate reference to the real PAN kept in a secure token vault
    @Column(name = "token", nullable = false, length = 64)
    String token;

    // Display-only masked PAN (e.g., 411111******1111); not sensitive by itself
    @Column(name = "masked_pan", length = 25)
    String maskedPan;


    // Card metadata allowed to store by PCI DSS
    @Column(name = "iin", length = 8)
    String iin; // first 6-8 digits (Issuer Identification Number)

    @Column(name = "last4", length = 4)
    String last4;

    @Column(name = "brand", length = 20)
    String brand; // VISA, MASTERCARD, RUPAY, etc.

    @Column(name = "expiry_month")
    Integer expiryMonth;

    @Column(name = "expiry_year")
    Integer expiryYear;

    // Status and linkage
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    CardStatus status = CardStatus.ACTIVE;
}

