package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.tenant.employee.entity.Employee; // Import Employee
import com.example.multi_tanent.tenant.employee.entity.EmployeeProfile;
import com.example.multi_tanent.tenant.employee.entity.JobDetails;
import com.example.multi_tanent.tenant.employee.repository.EmployeeProfileRepository;
import com.example.multi_tanent.tenant.employee.repository.JobDetailsRepository;
import com.example.multi_tanent.tenant.payroll.entity.CompanyInfo;
import com.example.multi_tanent.tenant.payroll.entity.Payslip;
import com.example.multi_tanent.tenant.payroll.entity.PayslipComponent;
import com.example.multi_tanent.tenant.payroll.enums.SalaryComponentType;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class PdfGenerationService {

    private final CompanyInfoService companyInfoService;
    private final JobDetailsRepository jobDetailsRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    public PdfGenerationService(
            CompanyInfoService companyInfoService,
            JobDetailsRepository jobDetailsRepository,
            EmployeeProfileRepository employeeProfileRepository) {
        this.companyInfoService = companyInfoService;
        this.jobDetailsRepository = jobDetailsRepository;
        this.employeeProfileRepository = employeeProfileRepository;
    }

    public byte[] generatePayslipPdf(Payslip payslip) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4)
        ) {
            document.setMargins(36, 36, 36, 36);

            CompanyInfo companyInfo = companyInfoService.getCompanyInfo();

            addHeader(document, payslip, companyInfo);
            document.add(new Paragraph("\n"));
            addEmployeeDetails(document, payslip);
            document.add(new Paragraph("\n"));
            addEarningsAndDeductions(document, payslip);
            document.add(new Paragraph("\n"));
            addNetPaySummary(document, payslip);
            document.add(new Paragraph("\n\n"));
            addFooter(document);

        } catch (Exception e) {
            // In a real app, you'd want more robust error handling
            throw new RuntimeException("Error generating PDF", e);
        }
        return baos.toByteArray();
    }

    private void addHeader(Document document, Payslip payslip, CompanyInfo companyInfo) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
        headerTable.setBorder(Border.NO_BORDER);

        // Company Info
        Cell companyCell = new Cell().setBorder(Border.NO_BORDER);
        if (companyInfo != null) {
            companyCell.add(new Paragraph(companyInfo.getCompanyName()).setBold().setFontSize(16));
            companyCell.add(new Paragraph(companyInfo.getAddress()).setFontSize(9));
            companyCell.add(new Paragraph(String.format("%s, %s - %s", companyInfo.getCity(), companyInfo.getState(), companyInfo.getPostalCode())).setFontSize(9));
        } else {
            companyCell.add(new Paragraph("Company Name").setBold().setFontSize(16));
        }
        headerTable.addCell(companyCell);

        // Payslip Title
        String monthName = payslip.getPayDate().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        Cell titleCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        titleCell.add(new Paragraph("Payslip").setBold().setFontSize(18));
        titleCell.add(new Paragraph(String.format("For %s %d", monthName, payslip.getYear())).setFontSize(10));
        headerTable.addCell(titleCell);

        document.add(headerTable);
        document.add(new Paragraph("_________________________________________________________________________").setBold().setMargin(0).setPadding(0));
    }

    private void addEmployeeDetails(Document document, Payslip payslip) {
        Employee employee = payslip.getEmployee();
        Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1})).useAllAvailableWidth();

        // Safely get job and profile details from the employee object
        // Find job and profile details from repositories
        JobDetails jobDetail = jobDetailsRepository.findByEmployeeId(employee.getId()).orElse(null);
        EmployeeProfile profile = employeeProfileRepository.findByEmployeeId(employee.getId()).orElse(null);

        String designationTitle = (jobDetail != null && jobDetail.getDesignation() != null) ? jobDetail.getDesignation() : "N/A";
        String departmentName = (jobDetail != null && jobDetail.getDepartment() != null) ? jobDetail.getDepartment() : "N/A";

        detailsTable.addCell(createDetailCell("Employee Name", employee.getFirstName() + " " + employee.getLastName()));
        detailsTable.addCell(createDetailCell("Designation", designationTitle));
        detailsTable.addCell(createDetailCell("Employee Code", employee.getEmployeeCode()));
        detailsTable.addCell(createDetailCell("Department", departmentName));

        // Use EmployeeProfile for bank details
        detailsTable.addCell(createDetailCell("Pay Date", payslip.getPayDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
        detailsTable.addCell(createDetailCell("Bank Name", (profile != null && profile.getBankName() != null) ? profile.getBankName() : "N/A"));
        detailsTable.addCell(createDetailCell("Account Number", (profile != null && profile.getBankAccountNumber() != null) ? profile.getBankAccountNumber() : "N/A"));

        document.add(detailsTable);
    }

    private Cell createDetailCell(String label, String value) {
        Cell cell = new Cell().setBorder(new SolidBorder(new DeviceGray(0.8f), 0.5f)).setPadding(5);
        cell.add(new Paragraph(label).setBold().setFontSize(8).setFontColor(ColorConstants.GRAY));
        cell.add(new Paragraph(value).setFontSize(9).setMargin(0));
        return cell;
    }

    private void addEarningsAndDeductions(Document document, Payslip payslip) {
        Table mainTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        mainTable.setBorder(Border.NO_BORDER);

        List<PayslipComponent> earnings = payslip.getComponents().stream()
                .filter(c -> c.getSalaryComponent().getType() == SalaryComponentType.EARNING || c.getSalaryComponent().getType() == SalaryComponentType.REIMBURSEMENT)
                .collect(Collectors.toList());

        List<PayslipComponent> deductions = payslip.getComponents().stream()
                .filter(c -> c.getSalaryComponent().getType() == SalaryComponentType.DEDUCTION || c.getSalaryComponent().getType() == SalaryComponentType.STATUTORY_CONTRIBUTION)
                .collect(Collectors.toList());

        mainTable.addCell(createSectionCell("Earnings", earnings, payslip.getGrossEarnings()));
        mainTable.addCell(createSectionCell("Deductions", deductions, payslip.getTotalDeductions()));

        document.add(mainTable);
    }

    private Cell createSectionCell(String title, List<PayslipComponent> components, BigDecimal total) {
        Cell cell = new Cell().setBorder(new SolidBorder(DeviceGray.BLACK, 1f)).setPadding(0);
        Table innerTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();

        Cell headerCell = new Cell(1, 2).add(new Paragraph(title).setBold()).setBackgroundColor(new DeviceGray(0.9f)).setPadding(5).setBorder(Border.NO_BORDER);
        innerTable.addHeaderCell(headerCell);

        for (PayslipComponent pc : components) {
            innerTable.addCell(new Cell().add(new Paragraph(pc.getSalaryComponent().getName())).setBorder(Border.NO_BORDER).setPadding(5));
            innerTable.addCell(new Cell().add(new Paragraph(String.format("%,.2f", pc.getAmount()))).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER).setPadding(5));
        }

        Cell totalLabelCell = new Cell().add(new Paragraph("Total " + title).setBold()).setBorderTop(new SolidBorder(DeviceGray.GRAY, 0.5f)).setPadding(5).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER).setBorderBottom(Border.NO_BORDER);
        Cell totalValueCell = new Cell().add(new Paragraph(String.format("%,.2f", total)).setBold()).setTextAlignment(TextAlignment.RIGHT).setBorderTop(new SolidBorder(DeviceGray.GRAY, 0.5f)).setPadding(5).setBorderLeft(Border.NO_BORDER).setBorderRight(Border.NO_BORDER).setBorderBottom(Border.NO_BORDER);
        innerTable.addFooterCell(totalLabelCell);
        innerTable.addFooterCell(totalValueCell);

        cell.add(innerTable);
        return cell;
    }

    private void addNetPaySummary(Document document, Payslip payslip) {
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
        summaryTable.addCell(new Cell().add(new Paragraph("Net Salary (Gross Earnings - Total Deductions)").setBold()).setBorder(Border.NO_BORDER).setPadding(5));
        summaryTable.addCell(new Cell().add(new Paragraph(String.format("₹ %,.2f", payslip.getNetSalary())).setBold().setFontSize(14)).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER).setPadding(5));
        document.add(summaryTable);
    }

    private void addFooter(Document document) {
        Paragraph footer = new Paragraph("This is a computer-generated payslip and does not require a signature.")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setFontSize(8);
        document.add(footer);
    }
}