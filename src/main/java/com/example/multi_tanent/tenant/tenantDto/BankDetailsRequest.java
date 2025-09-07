package com.example.multi_tanent.tenant.tenantDto;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankDetailsRequest {
    private String accountHolderName;
    private BigInteger accountNumber;
    private String ifscOrSwift;
    private String bankName;
    private String branch;
    private Boolean payoutActive;
}
