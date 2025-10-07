package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.tenant.payroll.entity.Payslip;
import com.example.multi_tanent.tenant.payroll.entity.PayslipComponent;
import com.example.multi_tanent.tenant.payroll.repository.PayslipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(transactionManager = "tenantTx")
public class PayslipService {

    private final PayslipRepository payslipRepository;

    public PayslipService(PayslipRepository payslipRepository) {
        this.payslipRepository = payslipRepository;
    }

    public List<Payslip> getPayslipsForEmployee(String employeeCode) {
        List<Payslip> payslips = payslipRepository.findByEmployeeEmployeeCodeOrderByYearDescMonthDesc(employeeCode);
        // Eagerly initialize the necessary associations within the transaction
        payslips.forEach(this::initializePayslipDetails);
        return payslips;
    }

    public Optional<Payslip> getPayslipById(Long id) {
        Optional<Payslip> payslipOpt = payslipRepository.findById(id);
        payslipOpt.ifPresent(this::initializePayslipDetails);
        return payslipOpt;
    }

    public List<Payslip> getPayslipsForPayrollRun(Long payrollRunId) {
        List<Payslip> payslips = payslipRepository.findByPayrollRunId(payrollRunId);
        payslips.forEach(this::initializePayslipDetails);
        return payslips;
    }

    /**
     * Initializes lazy-loaded associations of a Payslip entity to prevent LazyInitializationException.
     * This should be called within a @Transactional context.
     * @param payslip The Payslip entity to initialize.
     */
    private void initializePayslipDetails(Payslip payslip) {
        if (payslip == null) return;

        // Initialize the Employee proxy by accessing a property
        if (payslip.getEmployee() != null) {
            payslip.getEmployee().getEmployeeCode();
        }

        // Initialize the components and their associated SalaryComponent
        if (payslip.getComponents() != null) {
            payslip.getComponents().stream()
                    .map(PayslipComponent::getSalaryComponent)
                    .filter(Objects::nonNull)
                    .forEach(sc -> sc.getCode()); // Access a property to trigger load
        }
    }
}
