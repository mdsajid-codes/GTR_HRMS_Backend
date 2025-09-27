package com.example.multi_tanent.pos.service;

import com.example.multi_tanent.pos.dto.InvoiceDto;
import com.example.multi_tanent.pos.dto.InvoiceItemDto;
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
import java.time.format.DateTimeFormatter;

@Service
public class InvoicePdfService {

    public byte[] generateInvoicePdf(InvoiceDto invoice) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (
                PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf, PageSize.A4)
        ) {
            document.setMargins(36, 36, 36, 36);

            addHeader(document, invoice);
            document.add(new Paragraph("\n"));
            addCustomerDetails(document, invoice);
            document.add(new Paragraph("\n"));
            addItemsTable(document, invoice);
            document.add(new Paragraph("\n"));
            addTotals(document, invoice);
            document.add(new Paragraph("\n\n"));
            addFooter(document);

        } catch (Exception e) {
            throw new RuntimeException("Error generating invoice PDF", e);
        }
        return baos.toByteArray();
    }

    private void addHeader(Document document, InvoiceDto invoice) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{60, 40})).useAllAvailableWidth();
        headerTable.setBorder(Border.NO_BORDER);

        // Store Info
        Cell storeCell = new Cell().setBorder(Border.NO_BORDER);
        if (invoice.getStore() != null) {
            storeCell.add(new Paragraph(invoice.getStore().getName()).setBold().setFontSize(16));
            if (invoice.getStore().getAddress() != null) {
                storeCell.add(new Paragraph(invoice.getStore().getAddress()).setFontSize(9));
            }
        } else {
            storeCell.add(new Paragraph("Your Company").setBold().setFontSize(16));
        }
        headerTable.addCell(storeCell);

        // Invoice Title
        Cell titleCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        titleCell.add(new Paragraph("INVOICE").setBold().setFontSize(20).setFontColor(ColorConstants.GRAY));
        titleCell.add(new Paragraph("Invoice #: " + invoice.getInvoiceNo()).setFontSize(10));
        titleCell.add(new Paragraph("Date: " + invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).setFontSize(10));
        headerTable.addCell(titleCell);

        document.add(headerTable);
        document.add(new Paragraph("_________________________________________________________________________").setBold().setMargin(0).setPadding(0));
    }

    private void addCustomerDetails(Document document, InvoiceDto invoice) {
        if (invoice.getCustomer() == null) return;

        Paragraph billTo = new Paragraph("BILL TO").setBold().setFontSize(10).setFontColor(ColorConstants.GRAY);
        document.add(billTo);

        if (invoice.getCustomer().getName() != null) {
            document.add(new Paragraph(invoice.getCustomer().getName()).setBold());
        }
        if (invoice.getCustomer().getEmail() != null) {
            document.add(new Paragraph(invoice.getCustomer().getEmail()).setFontSize(9));
        }
        if (invoice.getCustomer().getPhone() != null) {
            document.add(new Paragraph(invoice.getCustomer().getPhone()).setFontSize(9));
        }
    }

    private void addItemsTable(Document document, InvoiceDto invoice) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 1, 1, 1})).useAllAvailableWidth();
        String currency = (invoice.getStore() != null && invoice.getStore().getCurrency() != null) ? invoice.getStore().getCurrency() : "AED";

        // Headers
        table.addHeaderCell(new Cell().add(new Paragraph("Item Description")).setBackgroundColor(DeviceGray.GRAY).setBold());
        table.addHeaderCell(new Cell().add(new Paragraph("Quantity")).setBackgroundColor(DeviceGray.GRAY).setBold().setTextAlignment(TextAlignment.RIGHT));
        table.addHeaderCell(new Cell().add(new Paragraph("Unit Price")).setBackgroundColor(DeviceGray.GRAY).setBold().setTextAlignment(TextAlignment.RIGHT));
        table.addHeaderCell(new Cell().add(new Paragraph("Total")).setBackgroundColor(DeviceGray.GRAY).setBold().setTextAlignment(TextAlignment.RIGHT));

        // Items
        for (InvoiceItemDto item : invoice.getItems()) {
            table.addCell(new Paragraph(item.getProductName() + "\n" + item.getVariantInfo()).setFontSize(9));
            table.addCell(new Paragraph(String.valueOf(item.getQuantity())).setTextAlignment(TextAlignment.RIGHT).setFontSize(9));
            table.addCell(new Paragraph(formatCurrency(item.getUnitPriceCents(), currency)).setTextAlignment(TextAlignment.RIGHT).setFontSize(9));
            table.addCell(new Paragraph(formatCurrency(item.getLineTotalCents(), currency)).setTextAlignment(TextAlignment.RIGHT).setFontSize(9));
        }

        document.add(table);
    }

    private void addTotals(Document document, InvoiceDto invoice) {
        Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{7, 2})).useAllAvailableWidth();
        totalsTable.setBorder(Border.NO_BORDER);
        String currency = (invoice.getStore() != null && invoice.getStore().getCurrency() != null) ? invoice.getStore().getCurrency() : "AED";

        totalsTable.addCell(createTotalCell("Subtotal", false));
        totalsTable.addCell(createTotalValueCell(formatCurrency(invoice.getSubtotalCents(), currency), false));

        totalsTable.addCell(createTotalCell("Tax", false));
        totalsTable.addCell(createTotalValueCell(formatCurrency(invoice.getTaxCents(), currency), false));

        if (invoice.getDiscountCents() > 0) {
            totalsTable.addCell(createTotalCell("Discount", false));
            totalsTable.addCell(createTotalValueCell("-" + formatCurrency(invoice.getDiscountCents(), currency), false));
        }

        totalsTable.addCell(createTotalCell("Total", true));
        totalsTable.addCell(createTotalValueCell(formatCurrency(invoice.getTotalCents(), currency), true));

        totalsTable.addCell(createTotalCell("Amount Paid", false));
        totalsTable.addCell(createTotalValueCell("-" + formatCurrency(invoice.getTotalPaidCents(), currency), false));

        totalsTable.addCell(createTotalCell("Amount Due", true));
        totalsTable.addCell(createTotalValueCell(formatCurrency(invoice.getAmountDueCents(), currency), true));

        document.add(totalsTable);
    }

    private Cell createTotalCell(String text, boolean isBold) {
        Cell cell = new Cell().add(new Paragraph(text)).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER).setPadding(2);
        return isBold ? cell.setBold() : cell;
    }

    private Cell createTotalValueCell(String text, boolean isBold) {
        Cell cell = new Cell().add(new Paragraph(text)).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER).setPadding(2);
        if (isBold) {
            cell.setBold().setBorderTop(new SolidBorder(DeviceGray.BLACK, 1f)).setBorderBottom(new SolidBorder(DeviceGray.BLACK, 1f));
        }
        return cell;
    }

    private void addFooter(Document document) {
        Paragraph footer = new Paragraph("Thank you for your business!")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setFontSize(9);
        document.add(footer);
    }

    private String formatCurrency(Long cents, String currency) {
        // A simple formatter. For production, consider using a more robust library.
        double amount = cents / 100.0;
        return String.format("%s %.2f", currency != null ? currency : "AED", amount);
    }
}