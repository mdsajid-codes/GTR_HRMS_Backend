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

                final BigDecimal[] grossEarnings = {BigDecimal.ZERO};
                final BigDecimal[] totalDeductions = {BigDecimal.ZERO};

                // --- Refactored Calculation Logic ---
                // Pass 1: Calculate prerequisite components (FLAT_AMOUNT and PERCENTAGE_OF_BASIC)
                BigDecimal basicAmount = BigDecimal.ZERO;
                for (SalaryStructureComponent ssc : salaryStructure.getComponents()) {
                    String code = ssc.getSalaryComponent().getCode();
                    CalculationType type = ssc.getSalaryComponent().getCalculationType();
                    BigDecimal value = ssc.getValue() != null ? ssc.getValue() : BigDecimal.ZERO;

                    if (type == CalculationType.FLAT_AMOUNT) {
                        calculatedAmounts.put(code, value);
                        if ("BASIC".equalsIgnoreCase(code)) {
                            basicAmount = value;
                        }
                    }
                }

                // Now that we have BASIC, we can calculate PERCENTAGE_OF_BASIC
                for (SalaryStructureComponent ssc : salaryStructure.getComponents()) {
                    if (ssc.getSalaryComponent().getCalculationType() == CalculationType.PERCENTAGE_OF_BASIC) {
                        BigDecimal percentage = ssc.getValue() != null ? ssc.getValue() : BigDecimal.ZERO;
                        BigDecimal calculatedValue = basicAmount.multiply(percentage).divide(new BigDecimal("100"));
                        calculatedAmounts.put(ssc.getSalaryComponent().getCode(), calculatedValue);
                    }
                }

                // Pass 2: Calculate initial gross earnings from already calculated components
                for (Map.Entry<String, BigDecimal> entry : calculatedAmounts.entrySet()) {
                    salaryComponentRepository.findByCode(entry.getKey()).ifPresent(sc -> {
                        if (sc.getType() == SalaryComponentType.EARNING && sc.isPartOfGrossSalary()) {
                            grossEarnings[0] = grossEarnings[0].add(entry.getValue());
                        }
                    });
                }

                // Pass 3: Calculate PERCENTAGE_OF_GROSS components
                for (SalaryStructureComponent ssc : salaryStructure.getComponents()) {
                    if (ssc.getSalaryComponent().getCalculationType() == CalculationType.PERCENTAGE_OF_GROSS) {
                        BigDecimal percentage = ssc.getValue() != null ? ssc.getValue() : BigDecimal.ZERO;
                        BigDecimal calculatedValue = grossEarnings[0].multiply(percentage).divide(new BigDecimal("100"));
                        calculatedAmounts.put(ssc.getSalaryComponent().getCode(), calculatedValue);
                    }
                }

                // Pass 4: Calculate FORMULA_BASED components
                StandardEvaluationContext context = new StandardEvaluationContext(calculatedAmounts);
                context.addPropertyAccessor(new MapAccessor());
                for (SalaryStructureComponent ssc : salaryStructure.getComponents()) {
                    if (ssc.getSalaryComponent().getCalculationType() == CalculationType.FORMULA_BASED && ssc.getFormula() != null) {
                        BigDecimal calculatedValue = expressionParser.parseExpression(ssc.getFormula()).getValue(context, BigDecimal.class);
                        calculatedAmounts.put(ssc.getSalaryComponent().getCode(), calculatedValue);
                    }
                }

                // Final Pass: Create PayslipComponents and calculate final totals
                grossEarnings[0] = BigDecimal.ZERO; // Reset and calculate final gross
                for (Map.Entry<String, BigDecimal> entry : calculatedAmounts.entrySet()) {
                    SalaryComponent sc = salaryComponentRepository.findByCode(entry.getKey()).orElseThrow();
                    PayslipComponent pc = new PayslipComponent();
                    pc.setPayslip(payslip);
                    pc.setSalaryComponent(sc);
                    pc.setAmount(entry.getValue());
                    components.add(pc);
                    if (sc.getType() == SalaryComponentType.EARNING && sc.isPartOfGrossSalary()) {
                        grossEarnings[0] = grossEarnings[0].add(entry.getValue());
                    } else if (sc.getType() == SalaryComponentType.DEDUCTION || sc.getType() == SalaryComponentType.STATUTORY_CONTRIBUTION) {
                        totalDeductions[0] = totalDeductions[0].add(entry.getValue());
                    }
                }

                EmployeeLoan loan = employeeIdToLoanMap.get(employee.getId());
                if (loan != null && loan.getRemainingInstallments() > 0) {
                    PayslipComponent loanPc = new PayslipComponent();
                    loanPc.setPayslip(payslip);
                    loanPc.setSalaryComponent(loanDeductionComponent);
                    loanPc.setAmount(loan.getEmiAmount().min(loan.getLoanAmount().subtract(loan.getEmiAmount().multiply(BigDecimal.valueOf(loan.getTotalInstallments() - loan.getRemainingInstallments()))))); // Ensure EMI doesn't exceed remaining loan amount
                    components.add(loanPc);
                    totalDeductions[0] = totalDeductions[0].add(loanPc.getAmount());

                    loan.setRemainingInstallments(loan.getRemainingInstallments() - 1);
                    if (loan.getRemainingInstallments() == 0) {
                        loan.setStatus(LoanStatus.PAID_OFF);
                    }
                    employeeLoanRepository.save(loan);
                }

                payslip.setGrossEarnings(grossEarnings[0]);
                payslip.setTotalDeductions(totalDeductions[0]);
                payslip.setNetSalary(grossEarnings[0].subtract(totalDeductions[0]));
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