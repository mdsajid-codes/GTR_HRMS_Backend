package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.payroll.dto.PayrollRunRequest;
import com.example.multi_tanent.tenant.payroll.entity.PayrollRun;
import com.example.multi_tanent.tenant.payroll.entity.Payslip;
import com.example.multi_tanent.tenant.payroll.enums.PayrollStatus;
import com.example.multi_tanent.tenant.payroll.repository.PayrollRunRepository;
import com.example.multi_tanent.tenant.payroll.repository.PayslipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(transactionManager = "tenantTx")
public class PayrollRunService {

    private final PayrollRunRepository payrollRunRepository;
    private final EmployeeRepository employeeRepository;
    private final PayslipRepository payslipRepository;
    private final PayslipGenerationService payslipGenerationService;

    public PayrollRunService(PayrollRunRepository payrollRunRepository, EmployeeRepository employeeRepository, PayslipRepository payslipRepository, PayslipGenerationService payslipGenerationService) {
        this.payrollRunRepository = payrollRunRepository;
        this.employeeRepository = employeeRepository;
        this.payslipRepository = payslipRepository;
        this.payslipGenerationService = payslipGenerationService;
    }

    public List<PayrollRun> getAllPayrollRuns() {
        return payrollRunRepository.findAll();
    }

    public Optional<PayrollRun> getPayrollRunById(Long id) {
        return payrollRunRepository.findById(id);
    }

    public PayrollRun createPayrollRun(PayrollRunRequest request) {
        payrollRunRepository.findByYearAndMonth(request.getYear(), request.getMonth()).ifPresent(run -> {
            throw new IllegalStateException("A payroll run for " + request.getMonth() + "/" + request.getYear() + " already exists.");
        });

        PayrollRun payrollRun = new PayrollRun();
        payrollRun.setYear(request.getYear());
        payrollRun.setMonth(request.getMonth());

        YearMonth yearMonth = YearMonth.of(request.getYear(), request.getMonth());
        payrollRun.setPayPeriodStart(yearMonth.atDay(1));
        payrollRun.setPayPeriodEnd(yearMonth.atEndOfMonth());

        payrollRun.setStatus(PayrollStatus.DRAFT);

        return payrollRunRepository.save(payrollRun);
    }

    public PayrollRun executePayrollRun(Long payrollRunId) {
        PayrollRun payrollRun = payrollRunRepository.findById(payrollRunId)
                .orElseThrow(() -> new RuntimeException("PayrollRun not found with id: " + payrollRunId));

        if (payrollRun.getStatus() != PayrollStatus.DRAFT) {
            throw new IllegalStateException("Payroll run can only be executed from DRAFT status. Current status: " + payrollRun.getStatus());
        }

        payrollRun.setStatus(PayrollStatus.GENERATED);
        payrollRun.setExecutedAt(LocalDateTime.now());

        List<Employee> activeEmployees = employeeRepository.findAll();
        payslipGenerationService.generatePayslipsForEmployees(payrollRun, activeEmployees);

        return payrollRunRepository.save(payrollRun);
    }

    public List<Payslip> getPayslipsForRun(Long payrollRunId) {
        return payslipRepository.findByPayrollRunId(payrollRunId);
    }
}