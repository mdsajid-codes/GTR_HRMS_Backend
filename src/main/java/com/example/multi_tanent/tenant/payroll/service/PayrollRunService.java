package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.tenant.payroll.dto.PayrollRunRequest;
import com.example.multi_tanent.tenant.payroll.entity.PayrollRun;
import com.example.multi_tanent.tenant.payroll.entity.Payslip;
import com.example.multi_tanent.tenant.payroll.enums.PayrollStatus;
import com.example.multi_tanent.tenant.payroll.repository.PayrollRunRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(transactionManager = "tenantTx")
public class PayrollRunService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayslipGenerationService payslipGenerationService;
    private final PayslipService payslipService;

    public PayrollRunService(PayrollRunRepository payrollRunRepository, PayslipGenerationService payslipGenerationService, PayslipService payslipService) {
        this.payrollRunRepository = payrollRunRepository;
        this.payslipGenerationService = payslipGenerationService;
        this.payslipService = payslipService;
    }

    public PayrollRun createPayrollRun(PayrollRunRequest request) {
        PayrollRun payrollRun = new PayrollRun();
        payrollRun.setYear(request.getYear());
        int month = request.getMonth();
        payrollRun.setMonth(month);

        // Automatically calculate pay period dates
        LocalDate payPeriodStart = LocalDate.of(request.getYear(), month, 1);
        LocalDate payPeriodEnd = payPeriodStart.withDayOfMonth(payPeriodStart.lengthOfMonth());
        payrollRun.setPayPeriodStart(payPeriodStart);
        payrollRun.setPayPeriodEnd(payPeriodEnd);
        payrollRun.setPayDate(request.getPayDate() != null ? request.getPayDate() : payPeriodEnd);
        payrollRun.setStatus(PayrollStatus.DRAFT);
        return payrollRunRepository.save(payrollRun);
    }

    public PayrollRun executePayrollRun(Long payrollRunId) {
        PayrollRun payrollRun = payrollRunRepository.findById(payrollRunId)
                .orElseThrow(() -> new EntityNotFoundException("PayrollRun not found with id: " + payrollRunId));

        if (payrollRun.getStatus() != PayrollStatus.DRAFT) {
            throw new IllegalStateException("Payroll run can only be executed from DRAFT status.");
        }

        payslipGenerationService.generatePayslipsForEmployees(payrollRun);

        payrollRun.setStatus(PayrollStatus.GENERATED);
        payrollRun.setExecutedAt(LocalDateTime.now());
        return payrollRunRepository.save(payrollRun);
    }

    public Payslip executePayrollForSingleEmployee(Long payrollRunId, Long employeeId) {
        PayrollRun payrollRun = payrollRunRepository.findById(payrollRunId)
                .orElseThrow(() -> new EntityNotFoundException("PayrollRun not found with id: " + payrollRunId));

        // The status of the main payroll run is not changed here, as this is an individual action.
        // We just use the payroll run for its period details.
        return payslipGenerationService.generatePayslipForSingleEmployee(payrollRun, employeeId);
    }

    @Transactional(readOnly = true)
    public List<PayrollRun> getAllPayrollRuns() {
        return payrollRunRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<PayrollRun> getPayrollRunById(Long id) {
        return payrollRunRepository.findById(id);
    }

    public List<Payslip> getPayslipsForRun(Long payrollRunId) {
        return payslipService.getPayslipsForPayrollRun(payrollRunId);
    }
}