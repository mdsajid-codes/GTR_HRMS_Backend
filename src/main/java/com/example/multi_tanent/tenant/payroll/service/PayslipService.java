package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.tenant.payroll.entity.Payslip;
import com.example.multi_tanent.tenant.payroll.repository.PayslipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(transactionManager = "tenantTx", readOnly = true)
public class PayslipService {

    private final PayslipRepository payslipRepository;

    public PayslipService(PayslipRepository payslipRepository) {
        this.payslipRepository = payslipRepository;
    }

    public List<Payslip> getPayslipsForEmployee(String employeeCode) {
        return payslipRepository.findByEmployeeEmployeeCodeOrderByYearDescMonthDesc(employeeCode);
    }

    public Optional<Payslip> getPayslipById(Long id) {
        return payslipRepository.findById(id);
    }
}