package com.example.multi_tanent.pos.controller;

import com.example.multi_tanent.pos.dto.report.BusinessSummaryDto;
import com.example.multi_tanent.pos.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/pos/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/business-summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'POS_ADMIN')")
    public ResponseEntity<BusinessSummaryDto> getBusinessSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(reportService.getBusinessSummary(fromDate, toDate));
    }

    @GetMapping("/business-summary/export")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'POS_ADMIN')")
    public ResponseEntity<InputStreamResource> exportBusinessSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        ByteArrayInputStream in = reportService.exportBusinessSummaryToExcel(fromDate, toDate);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=business_summary.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/daily-sales")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'POS_ADMIN')")
    public ResponseEntity<com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto> getDailySalesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reportService.getDailySalesSummary(date));
    }

    @GetMapping("/sales-by-hour")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'POS_ADMIN')")
    public ResponseEntity<List<com.example.multi_tanent.pos.dto.report.SalesByHourDto>> getSalesByHour(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(reportService.getSalesByHour(fromDate, toDate));
    }

    @GetMapping("/closing-reports")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'POS_ADMIN')")
    public ResponseEntity<List<com.example.multi_tanent.pos.dto.report.ClosingReportDto>> getClosingReports(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(reportService.getClosingReports(fromDate, toDate));
    }
}
