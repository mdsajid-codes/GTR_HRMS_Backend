package com.example.multi_tanent.tenant.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.multi_tanent.tenant.entity.Payroll;
import com.example.multi_tanent.tenant.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.repository.PayrollRepository;
import com.example.multi_tanent.tenant.service.PayslipService;
import com.example.multi_tanent.tenant.tenantDto.PayrollRequest;

@RestController
@RequestMapping("/api/payrolls")
@CrossOrigin(origins = "*")
public class PayrollController {
    private final EmployeeRepository employeeRepository;
    private final PayrollRepository payrollRepository;
    private final PayslipService payslipService;

    public PayrollController(EmployeeRepository employeeRepository, PayrollRepository payrollRepository, PayslipService payslipService){
        this.employeeRepository = employeeRepository;
        this.payrollRepository = payrollRepository;
        this.payslipService = payslipService;
    }

    @PostMapping("/{employeeCode}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<String> registerPayroll(@PathVariable String employeeCode, @RequestBody PayrollRequest payrollRequest){
        return employeeRepository.findByEmployeeCode(employeeCode)
                .map(employee -> {
                    // Check if a payroll already exists for the given employee and pay period
                    if (payrollRepository.findByEmployeeAndPayPeriodStartAndPayPeriodEnd(
                            employee, payrollRequest.getPayPeriodStart(), payrollRequest.getPayPeriodEnd()).isPresent()) {
                        return ResponseEntity.status(409).body("Payroll for this employee and pay period already exists.");
                    }

                    Payroll payroll = new Payroll();
                    payroll.setEmployee(employee);
                    payroll.setPayPeriodStart(payrollRequest.getPayPeriodStart());
                    payroll.setPayPeriodEnd(payrollRequest.getPayPeriodEnd());
                    payroll.setPayFrequency(payrollRequest.getPayFrequency());
                    payroll.setGrossSalary(payrollRequest.getGrossSalary());
                    payroll.setNetSalary(payrollRequest.getNetSalary());
                    payroll.setBasicSalary(payrollRequest.getBasicSalary());
                    payroll.setAllowances(payrollRequest.getAllowances());
                    payroll.setDeductions(payrollRequest.getDeductions());
                    payroll.setTaxAmount(payrollRequest.getTaxAmount());
                    payroll.setCurrency(payrollRequest.getCurrency());
                    payroll.setStatus(payrollRequest.getStatus());
                    payroll.setRemarks(payrollRequest.getRemarks());
                    payroll.setPayoutDate(LocalDate.now()); // Set payout date to current date
                    payroll.setCreatedAt(LocalDateTime.now());
                    payroll.setUpdatedAt(LocalDateTime.now());

                    payrollRepository.save(payroll);
                    return ResponseEntity.ok("Payroll registered successfully!");
                })
                .orElse(ResponseEntity.notFound().build());
                
    }

    @PutMapping("/{employeeCode}/{payrollId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<String> updatePayroll(@PathVariable String employeeCode, @PathVariable Long payrollId, @RequestBody PayrollRequest payrollRequest){
        return employeeRepository.findByEmployeeCode(employeeCode)
                .flatMap(employee -> payrollRepository.findById(payrollId)
                        .filter(payroll -> payroll.getEmployee().getId().equals(employee.getId())))
                .map(payroll -> {
                    payroll.setPayPeriodStart(payrollRequest.getPayPeriodStart());
                    payroll.setPayPeriodEnd(payrollRequest.getPayPeriodEnd());
                    payroll.setPayFrequency(payrollRequest.getPayFrequency());
                    payroll.setGrossSalary(payrollRequest.getGrossSalary());
                    payroll.setNetSalary(payrollRequest.getNetSalary());
                    payroll.setBasicSalary(payrollRequest.getBasicSalary());
                    payroll.setAllowances(payrollRequest.getAllowances());
                    payroll.setDeductions(payrollRequest.getDeductions());
                    payroll.setTaxAmount(payrollRequest.getTaxAmount());
                    payroll.setCurrency(payrollRequest.getCurrency());
                    payroll.setStatus(payrollRequest.getStatus());
                    payroll.setRemarks(payrollRequest.getRemarks());
                    payroll.setUpdatedAt(LocalDateTime.now());
                    payrollRepository.save(payroll);
                    return ResponseEntity.ok("Payroll updated successfully!");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{employeeCode}")
    public ResponseEntity<java.util.List<Payroll>> getPayrollsByEmployeeCode(@PathVariable String employeeCode){
        return employeeRepository.findByEmployeeCode(employeeCode)
                .map(employee -> ResponseEntity.ok(employee.getPayrolls().stream().toList()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{employeeCode}/by-start-date/{payPeriodStart}")
    public ResponseEntity<Payroll> getPayrollByEmployeeCodeAndPayPeriodStart(@PathVariable String employeeCode, @PathVariable LocalDate payPeriodStart){
        return payrollRepository.findByEmployee_EmployeeCodeAndPayPeriodStart(employeeCode, payPeriodStart)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{payrollId}/payslip")
    public ResponseEntity<byte[]> downloadPayslip(@PathVariable Long payrollId) {
        // Use the new repository method to fetch all required data eagerly
        return payrollRepository.findByIdWithEmployeeAndJobDetails(payrollId)
                .map(this::createPayslipResponse)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{employeeCode}/payslip/{year}/{month}")
    public ResponseEntity<byte[]> downloadPayslipByMonth(
            @PathVariable String employeeCode,
            @PathVariable int year,
            @PathVariable int month) {
        // Find the payroll record for the given employee, year, and month
        return payrollRepository.findByEmployeeCodeAndYearAndMonthWithDetails(employeeCode, year, month)
                .map(this::createPayslipResponse)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Helper method to generate a PDF response from a Payroll object.
     * @param payroll The payroll entity.
     * @return A ResponseEntity containing the PDF bytes or a 500 error.
     */
    private ResponseEntity<byte[]> createPayslipResponse(Payroll payroll) {
        try {
            byte[] pdfContents = payslipService.generatePayslipPdf(payroll);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = String.format("payslip-%s-%d-%02d.pdf", payroll.getEmployee().getEmployeeCode(), payroll.getPayPeriodStart().getYear(), payroll.getPayPeriodStart().getMonthValue());
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok().headers(headers).body(pdfContents);
        } catch (Exception e) {
            // In a real app, you should log this exception
            return ResponseEntity.status(500).build();
        }
    }
}
