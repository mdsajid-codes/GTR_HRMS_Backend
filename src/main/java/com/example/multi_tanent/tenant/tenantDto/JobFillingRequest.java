package com.example.multi_tanent.tenant.tenantDto;

import java.time.LocalDate;

import com.example.multi_tanent.tenant.entity.enums.BackgroundStatus;
import com.example.multi_tanent.tenant.entity.enums.HiringSource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobFillingRequest {
    private HiringSource hiringSource;
    private LocalDate offerDate;
    private LocalDate offerAcceptedDate;
    private LocalDate joiningDate;
    private BackgroundStatus backgroundStatus;
}
