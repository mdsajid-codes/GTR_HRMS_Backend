package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.spersusers.enitity.User;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.payroll.dto.ExpenseRequest;
import com.example.multi_tanent.tenant.payroll.entity.Expense;
import com.example.multi_tanent.tenant.payroll.enums.ExpenseStatus;
import com.example.multi_tanent.tenant.payroll.repository.ExpenseRepository;
import com.example.multi_tanent.tenant.service.FileStorageService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(transactionManager = "tenantTx")
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final EmployeeRepository employeeRepository;
    private final FileStorageService fileStorageService;

    public ExpenseService(ExpenseRepository expenseRepository, EmployeeRepository employeeRepository, FileStorageService fileStorageService) {
        this.expenseRepository = expenseRepository;
        this.employeeRepository = employeeRepository;
        this.fileStorageService = fileStorageService;
    }

    public Expense submitExpense(ExpenseRequest request, MultipartFile file) {
        Employee employee = employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + request.getEmployeeCode()));

        String receiptFileName = null;
        if (file != null && !file.isEmpty()) {
            // Store the file and get the generated file name
            receiptFileName = fileStorageService.storeFile(file, "expense_" + employee.getEmployeeCode());
        }

        Expense expense = new Expense();
        expense.setEmployee(employee);
        expense.setExpenseDate(request.getExpenseDate());
        expense.setCategory(request.getCategory());
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setBillNumber(request.getBillNumber());
        expense.setMerchentName(request.getMerchentName());
        expense.setReceiptPath(receiptFileName); // Save the file name to the database
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

    // public List<Expense> getExpensesByEmployeeCode(String employeeCode) {
    //     List<Expense> expenses = expenseRepository.findByEmployeeEmployeeCode(employeeCode);
    //     expenses.forEach(this::initializeExpenseDetails);
    //     return expenses;
    // }

    public Expense approveExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + expenseId));
        initializeExpenseDetails(expense);
        expense.setStatus(ExpenseStatus.APPROVED);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user != null) {
            expense.setProcessedByUserId(user.getId());
        }
        expense.setProcessedAt(LocalDateTime.now());
        return expenseRepository.save(expense);
    }

    public Expense rejectExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + expenseId));
        initializeExpenseDetails(expense);
        expense.setStatus(ExpenseStatus.REJECTED);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user != null) {
            expense.setProcessedByUserId(user.getId());
        }
        expense.setProcessedAt(LocalDateTime.now());
        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        // If there's an associated file, delete it from storage first.
        if (expense.getReceiptPath() != null && !expense.getReceiptPath().isEmpty()) {
            fileStorageService.deleteFile(expense.getReceiptPath());
        }
        expenseRepository.delete(expense);
    }

    private void initializeExpenseDetails(Expense expense) {
        if (expense.getEmployee() != null) {
            // Accessing a getter will trigger the lazy loading while the session is active.
            expense.getEmployee().getEmployeeCode();
        }
    }
}