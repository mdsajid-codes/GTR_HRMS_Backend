package com.example.multi_tanent.tenant.payroll.controller;

import com.example.multi_tanent.tenant.payroll.dto.ExpenseRequest;
import com.example.multi_tanent.tenant.payroll.dto.ExpenseResponse;
import com.example.multi_tanent.tenant.payroll.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExpenseResponse> submitExpense(@RequestBody ExpenseRequest request) {
        var newExpense = expenseService.submitExpense(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(newExpense.getId()).toUri();
        return ResponseEntity.created(location).body(ExpenseResponse.fromEntity(newExpense));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {
        List<ExpenseResponse> expenses = expenseService.getAllExpenses().stream()
                .map(ExpenseResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        return expenseService.getExpenseById(id)
                .map(expense -> ResponseEntity.ok(ExpenseResponse.fromEntity(expense)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByEmployeeCode(@PathVariable String employeeCode) {
        List<ExpenseResponse> expenses = expenseService.getExpensesByEmployeeCode(employeeCode).stream()
                .map(ExpenseResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(expenses);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
    public ResponseEntity<ExpenseResponse> approveExpense(@PathVariable Long id) {
        var approvedExpense = expenseService.approveExpense(id);
        return ResponseEntity.ok(ExpenseResponse.fromEntity(approvedExpense));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
    public ResponseEntity<ExpenseResponse> rejectExpense(@PathVariable Long id) {
        var rejectedExpense = expenseService.rejectExpense(id);
        return ResponseEntity.ok(ExpenseResponse.fromEntity(rejectedExpense));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
