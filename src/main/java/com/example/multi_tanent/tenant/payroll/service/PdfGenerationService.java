package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.tenant.base.entity.CompanyInfo;
import com.example.multi_tanent.tenant.employee.entity.EmployeeProfile;
import com.example.multi_tanent.tenant.employee.entity.JobDetails;
import com.example.multi_tanent.tenant.payroll.entity.EmployeeLoan;
import com.example.multi_tanent.tenant.payroll.dto.PayslipPdfData;
import com.example.multi_tanent.tenant.payroll.dto.FinalSettlementPdfData;
import com.example.multi_tanent.tenant.payroll.entity.EndOfService;
import com.example.multi_tanent.tenant.payroll.repository.EmployeeLoanRepository;
import com.example.multi_tanent.tenant.employee.repository.EmployeeProfileRepository;
import com.example.multi_tanent.spersusers.repository.TenantRepository;
import com.itextpdf.io.font.PdfEncodings;
// import com.example.multi_tanent.spersusers.service.TenantService;
import com.example.multi_tanent.tenant.employee.repository.JobDetailsRepository;
import com.example.multi_tanent.tenant.service.FileStorageService;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.example.multi_tanent.tenant.payroll.entity.Payslip;
import com.example.multi_tanent.tenant.payroll.entity.PayslipComponent;
import com.example.multi_tanent.tenant.payroll.enums.SalaryComponentType;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import com.itextpdf.io.image.ImageDataFactory;
import java.lang.Exception;

import java.io.ByteArrayOutputStream;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PdfGenerationService {
    private final CompanyInfoService companyInfoService;
    private final JobDetailsRepository jobDetailsRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final EmployeeLoanRepository employeeLoanRepository;
    private final FileStorageService fileStorageService;
    private final TenantRepository tenantRepository;
    private final PdfEosSettlement pdfEosSettlement;
    // No more repository injections needed here for payslip generation
    private PdfFont regularFont;
    private PdfFont boldFont;

    public PdfGenerationService(CompanyInfoService companyInfoService,
                                JobDetailsRepository jobDetailsRepository,
                                EmployeeProfileRepository employeeProfileRepository,
                                EmployeeLoanRepository employeeLoanRepository, // Still needed for Final Settlement
                                FileStorageService fileStorageService,
                                TenantRepository tenantRepository,
                                PdfEosSettlement pdfEosSettlement) { // Still needed for Final Settlement
        // In a real application, you would load a font that supports Arabic, like Arial or Noto Sans Arabic.
        // For this example, we will use a standard font and assume it can render the text.
        try {
            // Load a font that supports Arabic. Place the .ttf file in src/main/resources/fonts
            ClassPathResource fontRes = new ClassPathResource("fonts/NotoNaskhArabic-Regular.ttf");
            byte[] fontBytes = fontRes.getInputStream().readAllBytes();
            this.regularFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            this.boldFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED); // Using same font for bold for simplicity
        } catch (IOException e) {
           // Fallback to default font if the custom font is not found
            System.err.println("Custom Arabic font not found, falling back to default. Arabic text will not render correctly.");
            // throw new RuntimeException("Failed to load fonts for PDF generation", e);
        }
        this.companyInfoService = companyInfoService;
        this.jobDetailsRepository = jobDetailsRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.employeeLoanRepository = employeeLoanRepository;
        this.fileStorageService = fileStorageService;
        this.tenantRepository = tenantRepository;
        this.pdfEosSettlement = pdfEosSettlement;
    }

    public byte[] generatePayslipPdf(PayslipPdfData pdfData) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4)) {

            document.setMargins(36, 36, 36, 36);
            
            Payslip payslip = pdfData.getPayslip();

            addHeader(document, payslip, pdfData.getCompanyInfo(), pdfData.getTenant());
            document.add(new Paragraph("\n"));
            addEmployeeDetails(document, pdfData);
            addAttendanceSummary(document, payslip);
            addEarningsAndDeductions(document, payslip);
            document.add(new Paragraph("\n"));
            addNetPaySummary(document, payslip);
            document.add(new Paragraph("\n"));
            addPayslipSignatureSection(document);
            document.add(new Paragraph("\n"));
            addFooter(document);
        } catch (Exception e) {
            // In a real app, you'd want more robust error handling
            throw new RuntimeException("Error generating PDF", e);
        }
        return baos.toByteArray();
    }

    private void addHeader(Document document, Payslip payslip, CompanyInfo companyInfo, Tenant tenant) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{70, 30})).useAllAvailableWidth();
        headerTable.setBorder(Border.NO_BORDER);

        // Add company logo if it exists
        if (tenant != null && tenant.getLogoImgUrl() != null && !tenant.getLogoImgUrl().isEmpty()) {
            try {
                Resource logoResource = fileStorageService.loadFileAsResource(tenant.getLogoImgUrl());
                Image logo = new Image(ImageDataFactory.create(logoResource.getURL()));
                logo.setHeight(40); // Adjust size as needed
                logo.setWidth(120);
                document.add(logo);
            } catch (Exception e) {
                // Log the error but don't stop PDF generation
                // In a real app, you'd use a proper logger
                System.err.println("Could not load company logo: " + e.getMessage());
            }
        }

        // Company Info
        Cell companyCell = new Cell().setBorder(Border.NO_BORDER);
        if (companyInfo != null) {
            companyCell.add(new Paragraph(companyInfo.getCompanyName()).setBold().setFontSize(16));
            companyCell.add(new Paragraph(companyInfo.getAddress()).setFontSize(9));
            String city = companyInfo.getCity() != null ? companyInfo.getCity() : "";
            String state = companyInfo.getState() != null ? companyInfo.getState() : "";
            String postalCode = companyInfo.getPostalCode() != null ? companyInfo.getPostalCode() : "";
            companyCell.add(new Paragraph(String.format("%s, %s - %s", city, state, postalCode)).setFontSize(9));
        } else {
            companyCell.add(new Paragraph("Company Name").setBold().setFontSize(16));
        }
        headerTable.addCell(companyCell);

        // Payslip Title
        String monthName = payslip.getPayDate().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        Cell titleCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        titleCell.add(new Paragraph("PAYSLIP").setFont(boldFont).setFontSize(20).setFontColor(new DeviceGray(0.3f)));
        titleCell.add(new Paragraph(String.format("For %s %d", monthName, payslip.getYear())).setFontSize(10));
        headerTable.addCell(titleCell);

        document.add(headerTable);
        document.add(new Paragraph("_________________________________________________________________________").setBold().setMargin(0).setPadding(0));
    }

    private void addEmployeeDetails(Document document, PayslipPdfData pdfData) {
        Payslip payslip = pdfData.getPayslip();
        JobDetails jobDetail = pdfData.getJobDetails();
        EmployeeProfile profile = pdfData.getEmployeeProfile();

        Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1})).useAllAvailableWidth();

        String designationTitle = (jobDetail != null && jobDetail.getDesignation() != null) ? jobDetail.getDesignation() : "N/A";
        String departmentName = (jobDetail != null && jobDetail.getDepartment() != null) ? jobDetail.getDepartment() : "N/A";

        detailsTable.addCell(createDetailCell("Employee Name", pdfData.getEmployeeFullName()));
        detailsTable.addCell(createDetailCell("Designation", designationTitle));
        detailsTable.addCell(createDetailCell("Employee Code", payslip.getEmployee().getEmployeeCode()));
        detailsTable.addCell(createDetailCell("Department", departmentName));

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

    private void addAttendanceSummary(Document document, Payslip payslip) {
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1})).useAllAvailableWidth();

        summaryTable.addCell(createDetailCell("Month Days", String.valueOf(payslip.getTotalDaysInMonth())));
        summaryTable.addCell(createDetailCell("Payable Days", String.valueOf(payslip.getPayableDays())));
        summaryTable.addCell(createDetailCell("Loss of Pay Days", String.valueOf(payslip.getLossOfPayDays())));

        document.add(summaryTable);

        if (payslip.getLeaveBalanceSummary() != null && !payslip.getLeaveBalanceSummary().isEmpty()) {
            Paragraph leaveBalancePara = new Paragraph("Leave Balances: " + payslip.getLeaveBalanceSummary()).setFontSize(9).setItalic();
            document.add(leaveBalancePara);
        }
        document.add(new Paragraph("\n"));
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
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{25, 75})).useAllAvailableWidth();
        summaryTable.setBorder(new SolidBorder(DeviceGray.BLACK, 1f));

        Cell netLabelCell = new Cell().add(new Paragraph("Net Payable").setFont(boldFont).setFontSize(12))
                .setBorder(Border.NO_BORDER).setPadding(8).setBackgroundColor(new DeviceGray(0.9f));
        summaryTable.addCell(netLabelCell);

        Cell netValueCell = new Cell().add(new Paragraph(String.format("AED %,.2f", payslip.getNetSalary())).setFont(boldFont).setFontSize(14))
                .setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER).setPadding(8).setBackgroundColor(new DeviceGray(0.9f));
        summaryTable.addCell(netValueCell);

        // Amount in words - a full implementation is complex, so we'll add a placeholder for now.
        // In a real application, you would use a library or a dedicated service for this.
        Cell wordsLabelCell = new Cell().add(new Paragraph("Amount in Words").setFont(regularFont).setFontSize(9))
                .setBorder(Border.NO_BORDER).setPadding(8);
        summaryTable.addCell(wordsLabelCell);

        Cell wordsValueCell = new Cell().add(new Paragraph("AED " + " ... Only").setFont(regularFont).setFontSize(10).setItalic())
                .setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER).setPadding(8);
        summaryTable.addCell(wordsValueCell);

        document.add(summaryTable);
    }

    private void addFooter(Document document) {
        Paragraph footer = new Paragraph("This is a computer-generated payslip. This is a confidential document and is intended for the recipient only.")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setFontSize(8);
        document.add(footer);
    }

    public byte[] generateFinalSettlementPdf(FinalSettlementPdfData pdfData) {
        // Delegate the generation to the dedicated service
        return pdfEosSettlement.generate(pdfData);
    }

    private void addPayslipSignatureSection(Document document) {
        Table signatureTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
        signatureTable.setBorder(Border.NO_BORDER).setMarginTop(25);

        // Employee Signature
        Cell employeeSignCell = new Cell().setBorder(Border.NO_BORDER).setPadding(10);
        employeeSignCell.add(new Paragraph("\n\n\n").setMargin(0));
        employeeSignCell.add(new Paragraph("_________________________").setMargin(0));
        employeeSignCell.add(new Paragraph("Employee Signature").setFont(boldFont).setFontSize(9));
        signatureTable.addCell(employeeSignCell);

        // Company Signature
        Cell companySignCell = new Cell().setBorder(Border.NO_BORDER).setPadding(10).setTextAlignment(TextAlignment.RIGHT);
        companySignCell.add(new Paragraph("\n\n\n").setMargin(0));
        companySignCell.add(new Paragraph("_________________________").setMargin(0));
        companySignCell.add(new Paragraph("Authorised Signatory").setFont(boldFont).setFontSize(9));
        signatureTable.addCell(companySignCell);

        document.add(signatureTable);
    }
}