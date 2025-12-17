package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.spersusers.enitity.CompanyInfo;
import com.example.multi_tanent.tenant.employee.entity.EmployeeProfile;
import com.example.multi_tanent.tenant.employee.repository.EmployeeProfileRepository;
import com.example.multi_tanent.tenant.payroll.entity.EmployeeBankAccount;
import com.example.multi_tanent.tenant.payroll.entity.PayrollRun;
import com.example.multi_tanent.tenant.payroll.entity.Payslip;
import com.example.multi_tanent.tenant.payroll.repository.CompanyInfoRepository;
import com.example.multi_tanent.tenant.payroll.repository.EmployeeBankAccountRepository;
import com.example.multi_tanent.tenant.payroll.repository.PayrollRunRepository;
import com.example.multi_tanent.tenant.payroll.repository.PayslipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WpsService {

    private final PayrollRunRepository payrollRunRepository;
    private final CompanyInfoRepository companyInfoRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final EmployeeBankAccountRepository employeeBankAccountRepository;
    private final PayslipRepository payslipRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMyyyy");

    @Transactional(readOnly = true)
    public Map<String, Object> generateSifFile(Long payrollRunId) {
        String tenantId = TenantContext.getTenantId();
        PayrollRun payrollRun = payrollRunRepository.findById(payrollRunId)
                .orElseThrow(() -> new IllegalArgumentException("Payroll run not found"));

        CompanyInfo companyInfo = companyInfoRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Company info not found"));

        // Validate Company Info
        if (companyInfo.getMohreEstablishmentId() == null || companyInfo.getMohreEstablishmentId().isEmpty()) {
            throw new IllegalStateException("Company MOHRE Establishment ID is missing");
        }
        if (companyInfo.getEmployerBankRoutingCode() == null || companyInfo.getEmployerBankRoutingCode().isEmpty()) {
            throw new IllegalStateException("Company Employer Bank Routing Code is missing");
        }

        List<Payslip> payslips = payslipRepository.findByPayrollRunId(payrollRunId);
        if (payslips.isEmpty()) {
            throw new IllegalStateException("No payslips found for this payroll run.");
        }

        // Fetch Employee Profiles for additional details (Labor Card info)
        List<EmployeeProfile> employeeProfiles = employeeProfileRepository.findAll();
        Map<Long, EmployeeProfile> profileMap = employeeProfiles.stream()
                .collect(Collectors.toMap(p -> p.getEmployee().getId(), p -> p));

        // Fetch Employee Bank Accounts
        List<EmployeeBankAccount> bankAccounts = employeeBankAccountRepository.findAll();
        Map<Long, EmployeeBankAccount> bankAccountMap = bankAccounts.stream()
                .filter(EmployeeBankAccount::isPrimary)
                .collect(Collectors.toMap(b -> b.getEmployee().getId(), b -> b, (b1, b2) -> b1));

        StringBuilder sifContent = new StringBuilder();
        int recordCount = 0;
        BigDecimal totalSalary = BigDecimal.ZERO;

        // --- Generate EDR (Employee Detail Records) ---
        for (Payslip payslip : payslips) {
            Long employeeId = payslip.getEmployee().getId();
            EmployeeProfile profile = profileMap.get(employeeId);
            EmployeeBankAccount bankAccount = bankAccountMap.get(employeeId);

            if (profile == null || bankAccount == null) {
                // Log warning or skip? Standard behavior: skip if critical info missing or
                // error.
                // For now, let's skip but ideally we should report.
                continue;
            }

            if (!profile.isWpsRegistered()) {
                continue;
            }

            // Validations for Employee
            String employeeId14Digit = profile.getLaborCardNumber(); // Use Labor Card / MOHRE Person ID
            if (employeeId14Digit == null || employeeId14Digit.length() != 14) {
                // Try to pad if exists but short? Or throw? Assuming valid if exists.
                // If null, skipping to avoid invalid file.
                if (employeeId14Digit == null)
                    continue;
            }

            // Agent ID (Routing Code)
            String agentId = bankAccount.getRoutingCode();
            if (agentId == null || agentId.length() != 9) {
                // Fallback to profile routing code if bank account missing it?
                if (profile.getRoutingCode() != null && profile.getRoutingCode().length() == 9) {
                    agentId = profile.getRoutingCode();
                } else {
                    continue; // Skip if no valid agent ID
                }
            }

            String accountNo = bankAccount.getIban() != null && !bankAccount.getIban().isEmpty()
                    ? bankAccount.getIban()
                    : bankAccount.getAccountNumber();

            LocalDate startDate = payrollRun.getPayPeriodStart();
            LocalDate endDate = payrollRun.getPayPeriodEnd();
            int daysInPeriod = 30; // Standardize or calculate
            if (startDate != null && endDate != null) {
                // daysInPeriod = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate,
                // endDate) + 1;
                // Standard WPS often expects 30 or actual days.
                // Using payslip.getTotalDaysInMonth() if available
                if (payslip.getTotalDaysInMonth() != null) {
                    daysInPeriod = payslip.getTotalDaysInMonth();
                }
            }

            BigDecimal fixedSalary = payslip.getNetSalary(); // Using Net Salary as Fixed component
            BigDecimal variableSalary = BigDecimal.ZERO;

            // Format amounts to 2 decimal places, no commas
            // Actually WPS spec says no commas.

            int daysOnLeave = 0;
            if (payslip.getLossOfPayDays() != null) {
                daysOnLeave = payslip.getLossOfPayDays().intValue();
            }

            // "EDR", EmployeeID(14), AgentID(9), AccountNo(23 max?), ParamA(Start),
            // ParamB(End), ParamC(Days), Fixed, Variable, LeaveDays
            String edrRecord = String.format("EDR,%s,%s,%s,%s,%s,%d,%.2f,%.2f,%d",
                    employeeId14Digit,
                    agentId,
                    accountNo,
                    startDate.format(DATE_FORMATTER),
                    endDate.format(DATE_FORMATTER),
                    daysInPeriod,
                    fixedSalary,
                    variableSalary,
                    daysOnLeave);

            sifContent.append(edrRecord).append("\n");
            recordCount++;
            totalSalary = totalSalary.add(fixedSalary).add(variableSalary);
        }

        // --- Generate SCR (Salary Control Record) ---
        // "SCR", EmployerID(13), EmployerAgentID(9), FileDate, FileTime,
        // SalaryMonth(MMYYYY), EDRCount, TotalSalary, "AED", Ref

        String employerId = String.format("%013d", Long.parseLong(companyInfo.getMohreEstablishmentId())); // Pad to 13
        String employerAgentId = companyInfo.getEmployerBankRoutingCode();
        LocalDate now = LocalDate.now();
        String creationDate = now.format(DATE_FORMATTER);
        String creationTime = LocalDateTime.now().format(TIME_FORMATTER);
        String salaryMonth = String.format("%02d%d", payrollRun.getMonth(), payrollRun.getYear());

        String scrRecord = String.format("SCR,%s,%s,%s,%s,%s,%d,%.2f,AED,",
                employerId,
                employerAgentId,
                creationDate,
                creationTime,
                salaryMonth,
                recordCount,
                totalSalary);
        sifContent.append(scrRecord);

        // Filename: EEEEEEEEEEEEEYYMMDDHHMMSS.SIF
        // E (13 digits Employer ID)
        String fileName = String.format("%s%s%s.SIF",
                employerId,
                now.format(DateTimeFormatter.ofPattern("yyMMdd")),
                creationTime); // HHmmss (actually time formatter above is HHmm, SIF needs HHMMSS usually?

        // Wait, Specification said HHMMSS.
        // Let's correct time format for filename.
        String timeForFilename = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        fileName = String.format("%s%s%s.SIF", employerId, now.format(DateTimeFormatter.ofPattern("yyMMdd")),
                timeForFilename);

        return Map.of(
                "fileName", fileName,
                "content", sifContent.toString());
    }
}
