package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.tenant.employee.entity.Employee;
import com.example.multi_tanent.tenant.payroll.entity.*;
import com.example.multi_tanent.tenant.payroll.enums.CalculationType;
import com.example.multi_tanent.tenant.payroll.enums.LoanStatus;
import com.example.multi_tanent.tenant.payroll.enums.PayrollStatus;
import com.example.multi_tanent.tenant.payroll.enums.SalaryComponentType;
import com.example.multi_tanent.tenant.payroll.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(transactionManager = "tenantTx")
public class PayslipGenerationService {

    private final SalaryStructureRepository salaryStructureRepository;
    private final EmployeeLoanRepository employeeLoanRepository;
    private final PayslipRepository payslipRepository;
    private final SalaryComponentRepository salaryComponentRepository;

    public PayslipGenerationService(SalaryStructureRepository salaryStructureRepository, EmployeeLoanRepository employeeLoanRepository, PayslipRepository payslipRepository, SalaryComponentRepository salaryComponentRepository) {
        this.salaryStructureRepository = salaryStructureRepository;
        this.employeeLoanRepository = employeeLoanRepository;
        this.payslipRepository = payslipRepository;
        this.salaryComponentRepository = salaryComponentRepository;
    }

    public void generatePayslipsForEmployees(PayrollRun payrollRun, List<Employee> employees) {
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
                payslip.setPayDate(payrollRun.getPayPeriodEnd());
                payslip.setStatus(PayrollStatus.GENERATED);

                List<PayslipComponent> components = new ArrayList<>();
                BigDecimal grossEarnings = BigDecimal.ZERO;
                BigDecimal totalDeductions = BigDecimal.ZERO;

                for (SalaryStructureComponent ssc : salaryStructure.getComponents()) {
                    PayslipComponent pc = new PayslipComponent();
                    pc.setPayslip(payslip);
                    pc.setSalaryComponent(ssc.getSalaryComponent());
                    BigDecimal amount = ssc.getValue(); // Simplified calculation
                    pc.setAmount(amount);
                    components.add(pc);

                    if (ssc.getSalaryComponent().getType() == SalaryComponentType.EARNING) {
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