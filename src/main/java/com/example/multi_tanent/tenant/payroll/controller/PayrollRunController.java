package com.example.multi_tanent.tenant.payroll.controller;

import com.example.multi_tanent.tenant.payroll.dto.PayrollRunRequest;
import com.example.multi_tanent.tenant.payroll.dto.PayrollRunResponse;
import com.example.multi_tanent.tenant.payroll.dto.PayslipResponse;
import com.example.multi_tanent.tenant.payroll.service.PayrollRunService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payroll-runs")
@CrossOrigin(origins = "*")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
public class PayrollRunController {

    private final PayrollRunService payrollRunService;

    public PayrollRunController(PayrollRunService payrollRunService) {
        this.payrollRunService = payrollRunService;
    }

    @PostMapping
    public ResponseEntity<PayrollRunResponse> createPayrollRun(@RequestBody PayrollRunRequest request) {
        var createdRun = payrollRunService.createPayrollRun(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdRun.getId()).toUri();
        return ResponseEntity.created(location).body(PayrollRunResponse.fromEntity(createdRun));
    }

    @PostMapping("/{id}/execute/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR')")
    public ResponseEntity<PayslipResponse> executeForSingleEmployee(
            @PathVariable Long id,
            @PathVariable Long employeeId) {
        var payslip = payrollRunService.executePayrollForSingleEmployee(id, employeeId);
        return ResponseEntity.ok(PayslipResponse.fromEntity(payslip));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<PayrollRunResponse> executePayrollRun(@PathVariable Long id) {
        var executedRun = payrollRunService.executePayrollRun(id);
        return ResponseEntity.ok(PayrollRunResponse.fromEntity(executedRun));
    }

    @GetMapping
    public ResponseEntity<List<PayrollRunResponse>> getAllPayrollRuns() {
        List<PayrollRunResponse> runs = payrollRunService.getAllPayrollRuns().stream()
                .map(PayrollRunResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(runs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollRunResponse> getPayrollRunById(@PathVariable Long id) {
        return payrollRunService.getPayrollRunById(id)
                .map(run -> ResponseEntity.ok(PayrollRunResponse.fromEntity(run)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/payslips")
    public ResponseEntity<List<PayslipResponse>> getPayslipsForRun(@PathVariable Long id) {
        List<PayslipResponse> payslips = payrollRunService.getPayslipsForRun(id).stream()
                .map(PayslipResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(payslips);
    }
}
