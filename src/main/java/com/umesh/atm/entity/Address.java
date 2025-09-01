package com.umesh.atm.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Embeddable Address component for location details.
 * Can be embedded in other entities using @Embedded annotation.
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    /** First line of address */
    private String line1;

    /** Second line of address (optional) */
    private String line2;

    /** Third line of address (optional) */
    private String line3;

    /** Landmark reference (optional) */
    private String landmark;

    /** City name */
    private String city;

    /** District name */
    private String district;

    /** State name */
    private String state;

    /** Country name */
    private String country;

    /** PIN/ZIP code */
    private String pinCode;
}
