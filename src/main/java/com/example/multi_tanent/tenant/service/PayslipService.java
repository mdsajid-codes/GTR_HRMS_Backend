package com.example.multi_tanent.tenant.service;

import com.example.multi_tanent.tenant.entity.Employee;
import com.example.multi_tanent.tenant.entity.JobDetails;
import com.example.multi_tanent.tenant.entity.Payroll;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

@Service
public class PayslipService {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font BOLD_BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

    public byte[] generatePayslipPdf(Payroll payroll) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            // --- Header ---
            Paragraph title = new Paragraph("Payslip", TITLE_FONT);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n")); // Spacer

            Employee employee = payroll.getEmployee();

            // --- Employee Details Table ---
            document.add(createEmployeeDetailsTable(employee, payroll));

            // --- Earnings and Deductions Tables ---
            PdfPTable mainTable = new PdfPTable(2);
            mainTable.setWidthPercentage(100);
            mainTable.setSpacingBefore(20);

            mainTable.addCell(createEarningsTable(payroll));
            mainTable.addCell(createDeductionsTable(payroll));
            document.add(mainTable);

            // --- Summary Table ---
            document.add(createSummaryTable(payroll));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            // In a real app, you should log this exception with a proper logging framework
            throw new RuntimeException("Error generating payslip PDF", e);
        }
    }

    private PdfPTable createEmployeeDetailsTable(Employee employee, Payroll payroll) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 3f, 1.5f, 3f});

        table.addCell(createBorderlessCell("Employee Name:", BOLD_BODY_FONT, PdfPCell.ALIGN_LEFT));
        table.addCell(createBorderlessCell(employee.getFirstName() + " " + employee.getLastName(), BODY_FONT, PdfPCell.ALIGN_LEFT));
        table.addCell(createBorderlessCell("Pay Period:", BOLD_BODY_FONT, PdfPCell.ALIGN_LEFT));
        table.addCell(createBorderlessCell(payroll.getPayPeriodStart() + " to " + payroll.getPayPeriodEnd(), BODY_FONT, PdfPCell.ALIGN_LEFT));

        table.addCell(createBorderlessCell("Employee Code:", BOLD_BODY_FONT, PdfPCell.ALIGN_LEFT));
        table.addCell(createBorderlessCell(employee.getEmployeeCode(), BODY_FONT, PdfPCell.ALIGN_LEFT));
        table.addCell(createBorderlessCell("Payout Date:", BOLD_BODY_FONT, PdfPCell.ALIGN_LEFT));
        table.addCell(createBorderlessCell(String.valueOf(payroll.getPayoutDate()), BODY_FONT, PdfPCell.ALIGN_LEFT));

        String designation = Optional.ofNullable(employee.getJobDetails())
                .flatMap(jds -> jds.stream().findFirst())
                .map(JobDetails::getDesignationTitle)
                .orElse("N/A");

        table.addCell(createBorderlessCell("Designation:", BOLD_BODY_FONT, PdfPCell.ALIGN_LEFT));
        table.addCell(createBorderlessCell(designation, BODY_FONT, PdfPCell.ALIGN_LEFT));
        table.addCell(createBorderlessCell("", BODY_FONT, PdfPCell.ALIGN_LEFT)); // empty cell for alignment
        table.addCell(createBorderlessCell("", BODY_FONT, PdfPCell.ALIGN_LEFT)); // empty cell for alignment

        return table;
    }

    private PdfPCell createEarningsTable(Payroll payroll) {
        PdfPTable earningsTable = new PdfPTable(2);
        earningsTable.setWidths(new float[]{2f, 1f});

        PdfPCell header = new PdfPCell(new Phrase("Earnings", HEADER_FONT));
        header.setColspan(2);
        header.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        header.setBackgroundColor(new Color(220, 220, 220));
        header.setPadding(5);
        earningsTable.addCell(header);

        earningsTable.addCell(createLabelCell("Basic Salary"));
        earningsTable.addCell(createAmountCell(payroll.getBasicSalary(), payroll.getCurrency()));
        earningsTable.addCell(createLabelCell("Allowances"));
        earningsTable.addCell(createAmountCell(payroll.getAllowances(), payroll.getCurrency()));
        
        // Total Earnings
        earningsTable.addCell(createTotalLabelCell("Gross Earnings"));
        earningsTable.addCell(createTotalAmountCell(payroll.getGrossSalary(), payroll.getCurrency()));

        PdfPCell containerCell = new PdfPCell(earningsTable);
        containerCell.setPadding(0);
        return containerCell;
    }

    private PdfPCell createDeductionsTable(Payroll payroll) {
        PdfPTable deductionsTable = new PdfPTable(2);
        deductionsTable.setWidths(new float[]{2f, 1f});

        PdfPCell header = new PdfPCell(new Phrase("Deductions", HEADER_FONT));
        header.setColspan(2);
        header.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        header.setBackgroundColor(new Color(220, 220, 220));
        header.setPadding(5);
        deductionsTable.addCell(header);

        deductionsTable.addCell(createLabelCell("Tax (TDS)"));
        deductionsTable.addCell(createAmountCell(payroll.getTaxAmount(), payroll.getCurrency()));
        deductionsTable.addCell(createLabelCell("Other Deductions"));
        deductionsTable.addCell(createAmountCell(payroll.getDeductions(), payroll.getCurrency()));

        // Total Deductions
        double totalDeductions = (payroll.getTaxAmount() != null ? payroll.getTaxAmount() : 0) +
                                 (payroll.getDeductions() != null ? payroll.getDeductions() : 0);
        deductionsTable.addCell(createTotalLabelCell("Total Deductions"));
        deductionsTable.addCell(createTotalAmountCell(totalDeductions, payroll.getCurrency()));

        PdfPCell containerCell = new PdfPCell(deductionsTable);
        containerCell.setPadding(0);
        return containerCell;
    }

    private PdfPTable createSummaryTable(Payroll payroll) {
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingBefore(20);

        PdfPCell netPayLabelCell = new PdfPCell(new Phrase("Net Salary:", HEADER_FONT));
        netPayLabelCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        netPayLabelCell.setBorder(PdfPCell.NO_BORDER);
        netPayLabelCell.setPadding(5);
        summaryTable.addCell(netPayLabelCell);

        PdfPCell netPayValueCell = new PdfPCell(new Phrase(formatCurrency(payroll.getNetSalary(), payroll.getCurrency()), HEADER_FONT));
        netPayValueCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        netPayValueCell.setBorder(PdfPCell.NO_BORDER);
        netPayValueCell.setPadding(5);
        summaryTable.addCell(netPayValueCell);

        return summaryTable;
    }

    private PdfPCell createBorderlessCell(String content, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(4);
        return cell;
    }

    private PdfPCell createLabelCell(String content) {
        PdfPCell cell = new PdfPCell(new Phrase(content, BODY_FONT));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createAmountCell(Double amount, String currency) {
        String formattedAmount = (amount == null) ? "" : formatCurrency(amount, currency);
        PdfPCell cell = new PdfPCell(new Phrase(formattedAmount, BODY_FONT));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell createTotalLabelCell(String content) {
        PdfPCell cell = new PdfPCell(new Phrase(content, BOLD_BODY_FONT));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        cell.setPadding(5);
        cell.setBorderWidthTop(1);
        return cell;
    }

    private PdfPCell createTotalAmountCell(Double amount, String currency) {
        PdfPCell cell = new PdfPCell(new Phrase(formatCurrency(amount, currency), BOLD_BODY_FONT));
        cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
        cell.setPadding(5);
        cell.setBorderWidthTop(1);
        return cell;
    }

    private String formatCurrency(Double amount, String currencyCode) {
        if (amount == null) amount = 0.0;
        String symbol = "";
        if ("INR".equalsIgnoreCase(currencyCode)) {
            symbol = "₹ ";
        } else if ("USD".equalsIgnoreCase(currencyCode)) {
            symbol = "$ ";
        }
        return symbol + String.format("%,.2f", amount);
    }
}