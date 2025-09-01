package com.umesh.atm.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Base entity class providing common auditing fields and optimistic locking.
 * All domain entities should extend this class.
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {

    /** Version field for optimistic locking */
    @Version
    @Column(name = "version")
    private Long version;

    /** Timestamp when the entity was created */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp when the entity was last updated */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /** User who created the entity */
    @Column(name = "created_by", length = 50, updatable = false)
    private String createdBy;

    /** User who last updated the entity */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        // createdBy and updatedBy should be set by the service layer
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        // updatedBy should be set by the service layer
    }
}
