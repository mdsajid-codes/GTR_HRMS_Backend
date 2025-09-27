package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.tenant.employee.entity.Employee;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.payroll.dto.ExpenseRequest;
import com.example.multi_tanent.tenant.payroll.entity.Expense;
import com.example.multi_tanent.tenant.payroll.enums.ExpenseStatus;
import com.example.multi_tanent.tenant.payroll.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(transactionManager = "tenantTx")
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final EmployeeRepository employeeRepository;

    public ExpenseService(ExpenseRepository expenseRepository, EmployeeRepository employeeRepository) {
        this.expenseRepository = expenseRepository;
        this.employeeRepository = employeeRepository;
    }

    public Expense submitExpense(ExpenseRequest request) {
        Employee employee = employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + request.getEmployeeCode()));

        Expense expense = new Expense();
        expense.setEmployee(employee);
        expense.setExpenseDate(request.getExpenseDate());
        expense.setCategory(request.getCategory());
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setReceiptPath(request.getReceiptPath());
        expense.setStatus(ExpenseStatus.SUBMITTED);
        expense.setSubmittedAt(LocalDateTime.now());

        return expenseRepository.save(expense);
    }

    public List<Expense> getAllExpenses() {
        List<Expense> expenses = expenseRepository.findAll();
        expenses.forEach(this::initializeExpenseDetails);
        return expenses;
    }

    public Optional<Expense> getExpenseById(Long id) {
        Optional<Expense> expenseOpt = expenseRepository.findById(id);
        expenseOpt.ifPresent(this::initializeExpenseDetails);
        return expenseOpt;
    }

    public List<Expense> getExpensesByEmployeeCode(String employeeCode) {
        List<Expense> expenses = expenseRepository.findByEmployeeEmployeeCode(employeeCode);
        expenses.forEach(this::initializeExpenseDetails);
        return expenses;
    }

    public Expense approveExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + expenseId));
        initializeExpenseDetails(expense);
        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setProcessedAt(LocalDateTime.now());
        return expenseRepository.save(expense);
    }

    public Expense rejectExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + expenseId));
        initializeExpenseDetails(expense);
        expense.setStatus(ExpenseStatus.REJECTED);
        expense.setProcessedAt(LocalDateTime.now());
        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    private void initializeExpenseDetails(Expense expense) {
        if (expense.getEmployee() != null) {
            // Accessing a getter will trigger the lazy loading while the session is active.
            expense.getEmployee().getEmployeeCode();
        }
    }
}