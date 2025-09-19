package com.example.multi_tanent.tenant.payroll.dto;

import com.example.multi_tanent.tenant.payroll.entity.EmployeeLoan;
import com.example.multi_tanent.tenant.payroll.enums.LoanStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EmployeeLoanResponse {
    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private Long loanProductId;
    private String loanProductName;
    private BigDecimal loanAmount;
    private BigDecimal emiAmount;
    private Integer totalInstallments;
    private Integer remainingInstallments;
    private LocalDate startDate;
    private LoanStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    public static EmployeeLoanResponse fromEntity(EmployeeLoan loan) {
        if (loan == null) return null;
        EmployeeLoanResponse dto = new EmployeeLoanResponse();
        dto.setId(loan.getId());
        if (loan.getEmployee() != null) {
            dto.setEmployeeId(loan.getEmployee().getId());
            dto.setEmployeeCode(loan.getEmployee().getEmployeeCode());
            dto.setEmployeeName(loan.getEmployee().getFirstName() + " " + loan.getEmployee().getLastName());
        }
        if (loan.getLoanProduct() != null) {
            dto.setLoanProductId(loan.getLoanProduct().getId());
            dto.setLoanProductName(loan.getLoanProduct().getProductName());
        }
        dto.setLoanAmount(loan.getLoanAmount());
        dto.setEmiAmount(loan.getEmiAmount());
        dto.setTotalInstallments(loan.getTotalInstallments());
        dto.setRemainingInstallments(loan.getRemainingInstallments());
        dto.setStartDate(loan.getStartDate());
        dto.setStatus(loan.getStatus());
        dto.setRequestedAt(loan.getRequestedAt());
        dto.setProcessedAt(loan.getProcessedAt());
        return dto;
    }
}