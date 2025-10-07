package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.payroll.entity.*;
import com.example.multi_tanent.tenant.payroll.enums.CalculationType;
import com.example.multi_tanent.tenant.payroll.enums.LoanStatus;
import com.example.multi_tanent.tenant.payroll.enums.PayrollStatus;
import com.example.multi_tanent.tenant.payroll.enums.SalaryComponentType;
import org.springframework.context.expression.MapAccessor;
import com.example.multi_tanent.tenant.payroll.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(transactionManager = "tenantTx")
public class PayslipGenerationService {

    private final SalaryStructureRepository salaryStructureRepository;
    private final EmployeeLoanRepository employeeLoanRepository;
    private final EmployeeRepository employeeRepository;
    private final PayslipRepository payslipRepository;
    private final SalaryComponentRepository salaryComponentRepository;
    private final ExpressionParser expressionParser;

    public PayslipGenerationService(SalaryStructureRepository salaryStructureRepository,
                                    EmployeeLoanRepository employeeLoanRepository,
                                    EmployeeRepository employeeRepository, PayslipRepository payslipRepository,
                                    SalaryComponentRepository salaryComponentRepository) {
        this.salaryStructureRepository = salaryStructureRepository;
        this.employeeLoanRepository = employeeLoanRepository;
        this.employeeRepository = employeeRepository;
        this.payslipRepository = payslipRepository;
        this.salaryComponentRepository = salaryComponentRepository;
        this.expressionParser = new SpelExpressionParser();
    }

    public void generatePayslipsForEmployees(PayrollRun payrollRun) {
        List<Employee> employees = employeeRepository.findAll();

        List<EmployeeLoan> activeLoans = employeeLoanRepository.findByEmployeeInAndStatus(employees, LoanStatus.APPROVED);
        Map<Long, EmployeeLoan> employeeIdToLoanMap = activeLoans.stream()
                .collect(Collectors.toMap(loan -> loan.getEmployee().getId(), loan -> loan));

        SalaryComponent loanDeductionComponent = salaryComponentRepository.findByCode("LOAN_EMI")
                .orElseGet(this::createLoanEmiComponent);

        for (Employee employee : employees) {
            salaryStructureRepository.findByEmployeeId(employee.getId()).ifPresent(salaryStructure -> {
                Payslip payslip = new Payslip();
                payslip.setPayrollRun(payrollRun);
                payslip.setEmployee(employee);
                payslip.setYear(payrollRun.getYear());
                payslip.setMonth(payrollRun.getMonth());
                // Ensure payDate is never null. Fallback to pay period end date.
                LocalDate payDate = payrollRun.getPayDate() != null
                        ? payrollRun.getPayDate()
                        : payrollRun.getPayPeriodEnd();

                // If both are null, calculate it from the year and month as a final fallback.
                if (payDate == null && payrollRun.getYear() > 0 && payrollRun.getMonth() > 0) {
                    payDate = LocalDate.of(payrollRun.getYear(), payrollRun.getMonth(), 1).withDayOfMonth(LocalDate.of(payrollRun.getYear(), payrollRun.getMonth(), 1).lengthOfMonth());
                }
                payslip.setPayDate(payDate);
                payslip.setStatus(PayrollStatus.GENERATED);

                List<PayslipComponent> components = new ArrayList<>();
                Map<String, BigDecimal> calculatedAmounts = new HashMap<>();

                BigDecimal grossEarnings = BigDecimal.ZERO;
                BigDecimal totalDeductions = BigDecimal.ZERO;

                // First pass: Calculate all FLAT_AMOUNT components and store them in a map.
                for (SalaryStructureComponent ssc : salaryStructure.getComponents()) {
                    if (ssc.getSalaryComponent().getCalculationType() == CalculationType.FLAT_AMOUNT) {
                        BigDecimal amount = ssc.getValue() != null ? ssc.getValue() : BigDecimal.ZERO;
                        String componentCode = ssc.getSalaryComponent().getCode();
                        calculatedAmounts.put(componentCode, amount);
                    }
                }

                // Create the evaluation context with the calculated amounts as the root object.
                StandardEvaluationContext context = new StandardEvaluationContext(calculatedAmounts);
                context.addPropertyAccessor(new MapAccessor());

                // Second pass: Calculate all formula-based components
                for (SalaryStructureComponent ssc : salaryStructure.getComponents()) {
                    String componentCode = ssc.getSalaryComponent().getCode();
                    BigDecimal amount;

                    if (ssc.getSalaryComponent().getCalculationType() == CalculationType.FORMULA_BASED && ssc.getFormula() != null) {
                        // SpEL can now find 'BASIC' as a key in the root map.
                        amount = expressionParser.parseExpression(ssc.getFormula()).getValue(context, BigDecimal.class);
                    } else {
                        // This handles FLAT_AMOUNT and any others that might not have a formula
                        amount = calculatedAmounts.getOrDefault(componentCode, ssc.getValue() != null ? ssc.getValue() : BigDecimal.ZERO);
                    }

                    PayslipComponent pc = new PayslipComponent();
                    pc.setPayslip(payslip);
                    pc.setSalaryComponent(ssc.getSalaryComponent());
                    pc.setAmount(amount);
                    components.add(pc);

                    if (ssc.getSalaryComponent().getType() == SalaryComponentType.EARNING && ssc.getSalaryComponent().isPartOfGrossSalary()) {
                        grossEarnings = grossEarnings.add(amount);
                    } else if (ssc.getSalaryComponent().getType() == SalaryComponentType.DEDUCTION) {
                        totalDeductions = totalDeductions.add(amount);
                    }
                }

                EmployeeLoan loan = employeeIdToLoanMap.get(employee.getId());
                if (loan != null && loan.getRemainingInstallments() > 0) {
                    PayslipComponent loanPc = new PayslipComponent();
                    loanPc.setPayslip(payslip);
                    loanPc.setSalaryComponent(loanDeductionComponent);
                    loanPc.setAmount(loan.getEmiAmount());
                    components.add(loanPc);
                    totalDeductions = totalDeductions.add(loan.getEmiAmount());

                    loan.setRemainingInstallments(loan.getRemainingInstallments() - 1);
                    if (loan.getRemainingInstallments() == 0) {
                        loan.setStatus(LoanStatus.PAID_OFF);
                    }
                    employeeLoanRepository.save(loan);
                }

                payslip.setGrossEarnings(grossEarnings);
                payslip.setTotalDeductions(totalDeductions);
                payslip.setNetSalary(grossEarnings.subtract(totalDeductions));
                payslip.setComponents(components);

                payslipRepository.save(payslip);
            });
        }
    }

    private SalaryComponent createLoanEmiComponent() {
        SalaryComponent loanEmi = new SalaryComponent();
        loanEmi.setCode("LOAN_EMI");
        loanEmi.setName("Loan EMI");
        loanEmi.setType(SalaryComponentType.DEDUCTION);
        loanEmi.setTaxable(false);
        loanEmi.setPartOfGrossSalary(false);
        loanEmi.setCalculationType(CalculationType.FLAT_AMOUNT);
        return salaryComponentRepository.save(loanEmi);
    }
}