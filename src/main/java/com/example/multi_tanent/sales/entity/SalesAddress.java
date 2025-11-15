
package com.example.multi_tanent.sales.entity;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter@Setter
public class SalesAddress {
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

   
}
