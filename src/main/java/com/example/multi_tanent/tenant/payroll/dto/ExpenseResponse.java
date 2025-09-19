package com.example.multi_tanent.tenant.payroll.dto;

import com.example.multi_tanent.tenant.payroll.entity.Expense;
import com.example.multi_tanent.tenant.payroll.enums.ExpenseStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExpenseResponse {
    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private LocalDate expenseDate;
    private String category;
    private BigDecimal amount;
    private String description;
    private ExpenseStatus status;
    private String receiptPath;
    private LocalDateTime submittedAt;
    private LocalDateTime processedAt;

    public static ExpenseResponse fromEntity(Expense expense) {
        if (expense == null) return null;
        ExpenseResponse dto = new ExpenseResponse();
        dto.setId(expense.getId());
        if (expense.getEmployee() != null) {
            dto.setEmployeeId(expense.getEmployee().getId());
            dto.setEmployeeCode(expense.getEmployee().getEmployeeCode());
            dto.setEmployeeName(expense.getEmployee().getFirstName() + " " + expense.getEmployee().getLastName());
        }
        dto.setExpenseDate(expense.getExpenseDate());
        dto.setCategory(expense.getCategory());
        dto.setAmount(expense.getAmount());
        dto.setDescription(expense.getDescription());
        dto.setStatus(expense.getStatus());
        dto.setReceiptPath(expense.getReceiptPath());
        dto.setSubmittedAt(expense.getSubmittedAt());
        dto.setProcessedAt(expense.getProcessedAt());
        return dto;
    }
}