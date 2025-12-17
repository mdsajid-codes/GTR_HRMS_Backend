package com.example.multi_tanent.pos.service;

import com.example.multi_tanent.pos.dto.report.BusinessSummaryDto;
import com.example.multi_tanent.pos.entity.Sale;
import com.example.multi_tanent.pos.repository.SaleRepository;
import com.example.multi_tanent.spersusers.repository.TenantRepository;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Transactional("tenantTx")
public class ReportService {

    private final SaleRepository saleRepository;
    private final TenantRepository tenantRepository;
    private final com.example.multi_tanent.pos.repository.StockMovementRepository stockMovementRepository;

    public ReportService(SaleRepository saleRepository, TenantRepository tenantRepository,
            com.example.multi_tanent.pos.repository.StockMovementRepository stockMovementRepository) {
        this.saleRepository = saleRepository;
        this.tenantRepository = tenantRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    private Tenant getCurrentTenant() {
        return tenantRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Tenant context not found."));
    }

    public BusinessSummaryDto getBusinessSummary(LocalDate fromDate, LocalDate toDate) {
        Tenant tenant = getCurrentTenant();

        OffsetDateTime start = fromDate.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime end = toDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime()
                .minusNanos(1);

        List<Sale> sales = saleRepository.findByTenantIdAndInvoiceDateBetween(tenant.getId(), start, end);

        // 1. Sales General Summary
        long totalSubtotalCents = 0;
        long totalDelivery = 0;
        long totalDiscount = 0;
        long totalTax = 0;
        long totalGross = 0;

        for (Sale sale : sales) {
            if ("completed".equalsIgnoreCase(sale.getStatus()) || "paid".equalsIgnoreCase(sale.getPaymentStatus())) {
                totalSubtotalCents += sale.getSubtotalCents();
                totalDelivery += (sale.getDeliveryCharge() != null ? sale.getDeliveryCharge() : 0);
                totalDiscount += (sale.getDiscountCents() != null ? sale.getDiscountCents() : 0);
                totalTax += sale.getTaxCents();
                totalGross += sale.getTotalCents(); // Total is Sub + Tax - Disc + Delivery presumably, but checking
                                                    // logic: Total was calculated as sub + tax - disc. Delivery might
                                                    // be missing in calculation?
                // Checking SaleService logic: sale.setTotalCents(subtotal + totalTax -
                // sale.getDiscountCents());
                // It seems Delivery Charge was NOT included in Total Logic in SaleService?
                // Let's assume for report we aggregate fields as they are.
            }
        }

        // Re-calculating report metrics based on cents
        BigDecimal salesVal = toBigDecimal(totalSubtotalCents);
        BigDecimal deliveryVal = toBigDecimal(totalDelivery);
        BigDecimal discountVal = toBigDecimal(totalDiscount);
        BigDecimal vatVal = toBigDecimal(totalTax);

        BigDecimal grossSalesVal = salesVal.add(deliveryVal); // As per typical summary.
        BigDecimal netSalesIncVat = grossSalesVal.subtract(discountVal);
        BigDecimal netSalesExVat = netSalesIncVat.subtract(vatVal);

        BusinessSummaryDto.SalesSummary summary = new BusinessSummaryDto.SalesSummary(
                salesVal,
                deliveryVal,
                BigDecimal.ZERO, // Paid Modifiers
                grossSalesVal,
                discountVal,
                netSalesIncVat,
                vatVal,
                netSalesExVat,
                netSalesExVat // FnB sales minus VAT
        );

        // 2. Order Types
        Map<String, List<Sale>> byOrderType = sales.stream()
                .filter(s -> s.getOrderType() != null)
                .collect(Collectors.groupingBy(s -> s.getOrderType().name()));

        List<BusinessSummaryDto.OrderTypeSummary> orderTypes = new ArrayList<>();
        orderTypes.add(createOrderTypeSummary("Dine In", byOrderType.get("DINE_IN")));
        orderTypes.add(createOrderTypeSummary("Takeaway", byOrderType.get("TAKEAWAY")));
        orderTypes.add(createOrderTypeSummary("Delivery", byOrderType.get("DELIVERY")));

        // Calculate Totals for Order Types
        long totalOrders = orderTypes.stream().mapToLong(BusinessSummaryDto.OrderTypeSummary::getOrdersCount).sum();
        BigDecimal totalOrderValue = orderTypes.stream().map(BusinessSummaryDto.OrderTypeSummary::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orderTypes.add(new BusinessSummaryDto.OrderTypeSummary("Total", totalOrders, totalOrderValue));

        // 3. Sales Source
        Map<String, List<Sale>> bySource = sales.stream()
                .collect(Collectors.groupingBy(s -> s.getSalesSource() != null ? s.getSalesSource() : "Unknown"));

        List<BusinessSummaryDto.SalesSourceSummary> sources = new ArrayList<>();
        BigDecimal totalSourceAmount = BigDecimal.ZERO;
        long totalSourceQty = 0;

        for (Map.Entry<String, List<Sale>> entry : bySource.entrySet()) {
            BigDecimal amt = entry.getValue().stream()
                    .map(s -> toBigDecimal(s.getTotalCents()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long qty = entry.getValue().size();
            sources.add(new BusinessSummaryDto.SalesSourceSummary(entry.getKey(), qty, amt));

            totalSourceAmount = totalSourceAmount.add(amt);
            totalSourceQty += qty;
        }
        sources.add(new BusinessSummaryDto.SalesSourceSummary("Total", totalSourceQty, totalSourceAmount));

        // 4. Guest Count
        long adults = sales.stream().mapToLong(s -> s.getAdultsCount() != null ? s.getAdultsCount() : 0).sum();
        long kids = sales.stream().mapToLong(s -> s.getKidsCount() != null ? s.getKidsCount() : 0).sum();

        List<BusinessSummaryDto.GuestCountSummary> guests = new ArrayList<>();
        guests.add(new BusinessSummaryDto.GuestCountSummary("Adults Count", adults));
        guests.add(new BusinessSummaryDto.GuestCountSummary("Kids Count", kids));
        guests.add(new BusinessSummaryDto.GuestCountSummary("Total", adults + kids));

        // 5. Cost & Profit & Wastage
        // COGS = Sum of (SaleItem.costCents * quantity)
        long totalCogsCents = 0;
        for (Sale sale : sales) {
            if ("completed".equalsIgnoreCase(sale.getStatus()) || "paid".equalsIgnoreCase(sale.getPaymentStatus())) {
                for (com.example.multi_tanent.pos.entity.SaleItem item : sale.getItems()) {
                    long cost = item.getCostCents() != null ? item.getCostCents() : 0;
                    // item.getQuantity() is already multiplied in line totals usually, but cost is
                    // per unit?
                    // Checking SaleService: item.setCostCents(variant.getCostCents()) -> Unit cost.
                    totalCogsCents += (cost * item.getQuantity());
                }
            }
        }

        // Wastage from Stock Movements
        List<com.example.multi_tanent.pos.entity.StockMovement> wastageMovements = stockMovementRepository
                .findByTenantIdAndReasonContainingIgnoreCaseAndCreatedAtBetween(tenant.getId(), "Wastage", start, end);

        long totalWastageCents = 0;
        for (com.example.multi_tanent.pos.entity.StockMovement sm : wastageMovements) {
            // Wastage is usually a negative changeQuantity. We want the positive cost
            // value.
            long qty = Math.abs(sm.getChangeQuantity());
            // Need cost. StockMovement doesn't store cost snapshot?
            // It links to ProductVariant. We use current cost if snapshot missing.
            long unitCost = sm.getProductVariant().getCostCents();
            totalWastageCents += (qty * unitCost);
        }

        BigDecimal cogsVal = toBigDecimal(totalCogsCents);
        BigDecimal wastageVal = toBigDecimal(totalWastageCents);
        // Gross Profit = Net Sales (ex VAT) - COGS - Wastage (Optional: usually GP
        // don't include wastage, but user might want it)
        // Let's definition: Gross Profit = Net Sales (Ex VAT) - COGS.
        BigDecimal grossProfitVal = netSalesExVat.subtract(cogsVal);

        BusinessSummaryDto.CostProfitSummary costProfit = new BusinessSummaryDto.CostProfitSummary(cogsVal, wastageVal,
                grossProfitVal);
        BusinessSummaryDto.WastageSummary wastageSummary = new BusinessSummaryDto.WastageSummary(wastageVal);

        // 6. Collections (Payment Methods)
        Map<String, List<com.example.multi_tanent.pos.entity.Payment>> paymentsByMethod = sales.stream()
                .filter(s -> ("completed".equalsIgnoreCase(s.getStatus())
                        || "paid".equalsIgnoreCase(s.getPaymentStatus())) && s.getPayments() != null)
                .flatMap(s -> s.getPayments().stream())
                .collect(Collectors.groupingBy(p -> p.getMethod() != null ? p.getMethod() : "Unknown"));

        List<BusinessSummaryDto.CollectionsSummary> collections = new ArrayList<>();
        BigDecimal totalCollection = BigDecimal.ZERO;
        for (Map.Entry<String, List<com.example.multi_tanent.pos.entity.Payment>> entry : paymentsByMethod.entrySet()) {
            BigDecimal amt = entry.getValue().stream().map(p -> toBigDecimal(p.getAmountCents()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            collections.add(
                    new BusinessSummaryDto.CollectionsSummary(entry.getKey(), (long) entry.getValue().size(), amt));
            totalCollection = totalCollection.add(amt);
        }
        // Add total row
        // collections.add(new BusinessSummaryDto.CollectionsSummary("Total Collection",
        // 0L, totalCollection)); // Optional

        // 7. Tax Report
        // Group SaleItems by TaxRate
        // Map<RateName, List<SaleItem>>
        Map<String, List<com.example.multi_tanent.pos.entity.SaleItem>> itemsByTax = sales.stream()
                .filter(s -> "completed".equalsIgnoreCase(s.getStatus())
                        || "paid".equalsIgnoreCase(s.getPaymentStatus()))
                .flatMap(s -> s.getItems().stream())
                .collect(Collectors.groupingBy(i -> {
                    if (i.getProductVariant() != null && i.getProductVariant().getTaxRate() != null) {
                        return i.getProductVariant().getTaxRate().getName() + " ("
                                + i.getProductVariant().getTaxRate().getPercent() + "%)";
                    }
                    return "No Tax";
                }));

        List<BusinessSummaryDto.TaxReportSummary> taxReports = new ArrayList<>();
        for (Map.Entry<String, List<com.example.multi_tanent.pos.entity.SaleItem>> entry : itemsByTax.entrySet()) {
            BigDecimal salesAmt = entry.getValue().stream().map(i -> toBigDecimal(i.getLineTotalCents()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal taxAmt = entry.getValue().stream().map(i -> toBigDecimal(i.getTaxCents()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            // Percent is tricky to parse back from string, assuming user just wants display
            // name.
            // We can fetch percent from first item.
            BigDecimal percent = BigDecimal.ZERO;
            if (!entry.getValue().isEmpty()) {
                com.example.multi_tanent.pos.entity.SaleItem first = entry.getValue().get(0);
                if (first.getProductVariant() != null && first.getProductVariant().getTaxRate() != null) {
                    percent = first.getProductVariant().getTaxRate().getPercent();
                }
            }
            taxReports.add(new BusinessSummaryDto.TaxReportSummary(entry.getKey(), percent, salesAmt, taxAmt));
        }

        // 8. Staff Report
        Map<String, List<Sale>> byStaff = sales.stream()
                .filter(s -> ("completed".equalsIgnoreCase(s.getStatus())
                        || "paid".equalsIgnoreCase(s.getPaymentStatus())) && s.getUser() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getUser().getName() != null ? s.getUser().getName() : s.getUser().getEmail())); // Fallback
                                                                                                               // to
                                                                                                               // email

        List<BusinessSummaryDto.StaffReportSummary> staffReports = new ArrayList<>();
        for (Map.Entry<String, List<Sale>> entry : byStaff.entrySet()) {
            long qty = entry.getValue().size();
            BigDecimal amt = entry.getValue().stream().map(s -> toBigDecimal(s.getTotalCents())).reduce(BigDecimal.ZERO,
                    BigDecimal::add);
            staffReports.add(new BusinessSummaryDto.StaffReportSummary(entry.getKey(), qty, amt));
        }

        // 9. Category Report
        // Group SaleItems by Category
        Map<String, List<com.example.multi_tanent.pos.entity.SaleItem>> itemsByCategory = sales.stream()
                .filter(s -> "completed".equalsIgnoreCase(s.getStatus())
                        || "paid".equalsIgnoreCase(s.getPaymentStatus()))
                .flatMap(s -> s.getItems().stream())
                .collect(Collectors.groupingBy(i -> {
                    if (i.getProductVariant() != null && i.getProductVariant().getProduct() != null
                            && i.getProductVariant().getProduct().getCategory() != null) {
                        return i.getProductVariant().getProduct().getCategory().getName();
                    }
                    return "Uncategorized";
                }));

        List<BusinessSummaryDto.CategoryReportSummary> categoryReports = new ArrayList<>();
        for (Map.Entry<String, List<com.example.multi_tanent.pos.entity.SaleItem>> entry : itemsByCategory.entrySet()) {
            long qty = entry.getValue().stream().mapToLong(com.example.multi_tanent.pos.entity.SaleItem::getQuantity)
                    .sum();
            BigDecimal amt = entry.getValue().stream().map(i -> toBigDecimal(i.getLineTotalCents()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add); // Sales amount for category
            categoryReports.add(new BusinessSummaryDto.CategoryReportSummary(entry.getKey(), qty, amt));
        }

        return new BusinessSummaryDto(summary, orderTypes, sources, guests, costProfit, collections, taxReports,
                staffReports, categoryReports, wastageSummary);
    }

    private BusinessSummaryDto.OrderTypeSummary createOrderTypeSummary(String label, List<Sale> typeSales) {
        if (typeSales == null || typeSales.isEmpty()) {
            return new BusinessSummaryDto.OrderTypeSummary(label, 0L, BigDecimal.ZERO);
        }
        BigDecimal value = typeSales.stream()
                .map(s -> toBigDecimal(s.getTotalCents())) // Using Total Cents for value
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new BusinessSummaryDto.OrderTypeSummary(label, (long) typeSales.size(), value);
    }

    private BigDecimal toBigDecimal(Long cents) {
        if (cents == null)
            return BigDecimal.ZERO;
        return BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100));
    }

    public ByteArrayInputStream exportBusinessSummaryToExcel(LocalDate fromDate, LocalDate toDate) {
        BusinessSummaryDto data = getBusinessSummary(fromDate, toDate);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Sheet 1: Sales Summary
            Sheet summarySheet = workbook.createSheet("Sales Summary");
            Row headerRow = summarySheet.createRow(0);
            headerRow.createCell(0).setCellValue("Metric");
            headerRow.createCell(1).setCellValue("Value");

            int rowIdx = 1;
            BusinessSummaryDto.SalesSummary s = data.getSalesSummary();
            addRow(summarySheet, rowIdx++, "Sales", s.getSales());
            addRow(summarySheet, rowIdx++, "Delivery", s.getDeliveryCharge());
            addRow(summarySheet, rowIdx++, "Paid Modifiers", s.getPaidModifiers());
            addRow(summarySheet, rowIdx++, "Gross Sales", s.getGrossSales());
            addRow(summarySheet, rowIdx++, "Discount", s.getDiscounts());
            addRow(summarySheet, rowIdx++, "Net Sales (Inc VAT)", s.getNetSalesIncludingVat());
            addRow(summarySheet, rowIdx++, "VAT", s.getVat());
            addRow(summarySheet, rowIdx++, "Net Sales (Ex VAT)", s.getNetSalesExcludingVat());

            // Sheet 2: Order Types
            Sheet orderTypeSheet = workbook.createSheet("Order Types");
            headerRow = orderTypeSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Order Type");
            headerRow.createCell(1).setCellValue("Orders Count");
            headerRow.createCell(2).setCellValue("Value");

            rowIdx = 1;
            for (BusinessSummaryDto.OrderTypeSummary ot : data.getOrderTypes()) {
                Row row = orderTypeSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(ot.getOrderType());
                row.createCell(1).setCellValue(ot.getOrdersCount());
                row.createCell(2).setCellValue(ot.getValue().doubleValue());
            }

            // Sheet 3: Sales Source
            Sheet sourceSheet = workbook.createSheet("Sales Source");
            headerRow = sourceSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Source");
            headerRow.createCell(1).setCellValue("Orders Count");
            headerRow.createCell(2).setCellValue("Amount");
            rowIdx = 1;

            for (BusinessSummaryDto.SalesSourceSummary ss : data.getSalesSources()) {
                Row row = sourceSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(ss.getSalesSource());
                row.createCell(1).setCellValue(ss.getQuantity());
                row.createCell(2).setCellValue(ss.getAmount().doubleValue());
            }

            // Sheet 4: Guest Count
            Sheet guestSheet = workbook.createSheet("Guest Count");
            headerRow = guestSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Type");
            headerRow.createCell(1).setCellValue("Count");
            rowIdx = 1;
            for (BusinessSummaryDto.GuestCountSummary gc : data.getGuestCounts()) {
                Row row = guestSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(gc.getDescription());
                row.createCell(1).setCellValue(gc.getCount());
            }

            // Sheet 5: Cost & Profit
            Sheet costSheet = workbook.createSheet("Cost & Profit");
            headerRow = costSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Metric");
            headerRow.createCell(1).setCellValue("Value");
            rowIdx = 1;
            BusinessSummaryDto.CostProfitSummary cp = data.getCostProfit();
            addRow(costSheet, rowIdx++, "COGS", cp.getCogs());
            addRow(costSheet, rowIdx++, "Wastage", cp.getWastage());
            addRow(costSheet, rowIdx++, "Gross Profit", cp.getGrossProfit());

            // Sheet 6: Collections
            Sheet paymentSheet = workbook.createSheet("Collections");
            headerRow = paymentSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Method");
            headerRow.createCell(1).setCellValue("Transactions");
            headerRow.createCell(2).setCellValue("Amount");
            rowIdx = 1;
            for (BusinessSummaryDto.CollectionsSummary cs : data.getCollections()) {
                Row row = paymentSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(cs.getMethod());
                row.createCell(1).setCellValue(cs.getQuantity());
                row.createCell(2).setCellValue(cs.getAmount().doubleValue());
            }

            // Sheet 7: Tax
            Sheet taxSheet = workbook.createSheet("Tax Report");
            headerRow = taxSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Tax");
            headerRow.createCell(1).setCellValue("Rate %");
            headerRow.createCell(2).setCellValue("Sales Amount");
            headerRow.createCell(3).setCellValue("Tax Amount");
            rowIdx = 1;
            for (BusinessSummaryDto.TaxReportSummary tr : data.getTaxReports()) {
                Row row = taxSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(tr.getRateName());
                row.createCell(1).setCellValue(tr.getRatePercent().doubleValue());
                row.createCell(2).setCellValue(tr.getSalesAmount().doubleValue());
                row.createCell(3).setCellValue(tr.getTaxAmount().doubleValue());
            }

            // Sheet 8: Staff
            Sheet staffSheet = workbook.createSheet("Staff Report");
            headerRow = staffSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Staff");
            headerRow.createCell(1).setCellValue("Orders");
            headerRow.createCell(2).setCellValue("Amount");
            rowIdx = 1;
            for (BusinessSummaryDto.StaffReportSummary sr : data.getStaffReports()) {
                Row row = staffSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(sr.getStaffName());
                row.createCell(1).setCellValue(sr.getQuantity());
                row.createCell(2).setCellValue(sr.getAmount().doubleValue());
            }

            // Sheet 9: Category
            Sheet catSheet = workbook.createSheet("Category Report");
            headerRow = catSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Category");
            headerRow.createCell(1).setCellValue("Qty Sold");
            headerRow.createCell(2).setCellValue("Sales Amount");
            rowIdx = 1;
            for (BusinessSummaryDto.CategoryReportSummary cr : data.getCategoryReports()) {
                Row row = catSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(cr.getCategoryName());
                row.createCell(1).setCellValue(cr.getQuantity());
                row.createCell(2).setCellValue(cr.getAmount().doubleValue());
            }

            // Sheet 10: Wastage
            Sheet wastageSheet = workbook.createSheet("Wastage");
            headerRow = wastageSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Total Wastage Cost");

            rowIdx = 1;
            Row wRow = wastageSheet.createRow(rowIdx++);
            wRow.createCell(0).setCellValue(data.getWastage().getTotalWastageCost().doubleValue());

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export Excel data: " + e.getMessage());
        }
    }

    private void addRow(Sheet sheet, int rowIdx, String label, BigDecimal value) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value != null ? value.doubleValue() : 0.0);
    }

    public com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto getDailySalesSummary(LocalDate date) {
        Tenant tenant = getCurrentTenant();
        OffsetDateTime start = date.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime end = date.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime()
                .minusNanos(1);

        List<Sale> allSales = saleRepository.findByTenantIdAndInvoiceDateBetween(tenant.getId(), start, end);

        com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto dto = new com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto();
        dto.setDate(date.toString());
        dto.setTime(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        dto.setStoreName(tenant.getName() != null ? tenant.getName() : "My Store");

        // 1. Filter for valid sales (completed/paid/partial) vs cancelled
        List<Sale> validSales = allSales.stream()
                .filter(s -> !"cancelled".equalsIgnoreCase(s.getStatus()))
                .collect(Collectors.toList());
        List<Sale> cancelledSales = allSales.stream()
                .filter(s -> "cancelled".equalsIgnoreCase(s.getStatus()))
                .collect(Collectors.toList());

        // 2. Aggregates for Cash Report -> Sales Section
        long subTotalQty = validSales.size(); // Or sum items? Image implies "Sales" count. Let's use count of sales for
                                              // now.
        long totalSubtotalCents = validSales.stream().mapToLong(Sale::getSubtotalCents).sum();
        long totalDiscountCents = validSales.stream()
                .mapToLong(s -> s.getDiscountCents() != null ? s.getDiscountCents() : 0).sum();
        long totalDeliveryCents = validSales.stream()
                .mapToLong(s -> s.getDeliveryCharge() != null ? s.getDeliveryCharge() : 0).sum();
        long totalTaxCents = validSales.stream().mapToLong(Sale::getTaxCents).sum();
        long totalBillCents = validSales.stream().mapToLong(Sale::getTotalCents).sum();

        com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.SalesSection salesSection = new com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.SalesSection();
        salesSection.setSubTotalQty(subTotalQty);
        salesSection.setSubTotalAmount(toBigDecimal(totalSubtotalCents));
        salesSection.setDiscount(toBigDecimal(totalDiscountCents));
        salesSection.setShippingCharge(toBigDecimal(totalDeliveryCents));
        salesSection.setVat(toBigDecimal(totalTaxCents));
        salesSection.setBillAmount(toBigDecimal(totalBillCents));

        // 3. Collection Section
        // Assuming "Net Collection" is total paid amount.
        // We need to check Payments if available, or assume TotalCents for
        // paid/completed sales.
        // For accurate collection, we should sum up Payment entities if they exist.
        // Since Sale entity has getPayments(), we use that if initialized.
        // However, standard sales fetch might not fetch payments eagerly.
        // Let's assume for this report we sum 'TotalCents' of valid sales as 'Net
        // Collection' for now,
        // or refine if Payment entity is available. The provided Sale entity has
        // List<Payment>.

        long totalCollectedCents = validSales.stream()
                .mapToLong(s -> {
                    if (s.getPayments() != null && !s.getPayments().isEmpty()) {
                        return s.getPayments().stream()
                                .mapToLong(p -> p.getAmountCents() != null ? p.getAmountCents() : 0).sum();
                    } else {
                        // Fallback: if completed/paid, assume partial/full payment logic or just 0 if
                        // no payment record?
                        // Usually if status is 'paid', total was collected.
                        return "paid".equalsIgnoreCase(s.getPaymentStatus()) ? s.getTotalCents() : 0;
                    }
                }).sum();

        com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.CollectionSection collectionSection = new com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.CollectionSection();
        collectionSection.setNetCollection(toBigDecimal(totalCollectedCents));
        collectionSection.setTipAmount(BigDecimal.ZERO); // Not tracked
        collectionSection.setOnAccount(BigDecimal.ZERO); // Not tracked specifically yet

        // 4. Others Section
        com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.OthersSection othersSection = new com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.OthersSection();
        othersSection.setVatQty(0L); // Usually count of tax items?
        othersSection.setVatAmount(toBigDecimal(totalTaxCents));
        othersSection.setDeliveryChargesQty(0L);
        othersSection.setDeliveryChargesAmount(toBigDecimal(totalDeliveryCents));
        othersSection.setTotal(toBigDecimal(totalTaxCents + totalDeliveryCents));

        // 5. Discount Details
        com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.DiscountDetailsSection discountSection = new com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.DiscountDetailsSection();
        discountSection.setTotal(toBigDecimal(totalDiscountCents));

        com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.CashReportSection cashReport = new com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.CashReportSection();
        cashReport.setSales(salesSection);
        cashReport.setCollection(collectionSection);
        cashReport.setOthers(othersSection);
        cashReport.setDiscountDetails(discountSection);
        dto.setCashReport(cashReport);

        // 6. Tax Report
        // Group sale items by tax rate
        // We need to iterate all items of valid sales.
        Map<String, List<com.example.multi_tanent.pos.entity.SaleItem>> itemsByTax = validSales.stream()
                .flatMap(s -> s.getItems().stream())
                .collect(Collectors.groupingBy(item -> {
                    if (item.getProductVariant() != null && item.getProductVariant().getTaxRate() != null) {
                        return item.getProductVariant().getTaxRate().getName() + " ("
                                + item.getProductVariant().getTaxRate().getPercent() + "%)";
                    }
                    return "No Tax";
                }));

        List<com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.TaxReportItem> taxReports = new ArrayList<>();
        itemsByTax.forEach((rateName, items) -> {
            long taxableAmountCents = items.stream()
                    .mapToLong(com.example.multi_tanent.pos.entity.SaleItem::getLineTotalCents).sum();
            long taxValCents = items.stream().mapToLong(com.example.multi_tanent.pos.entity.SaleItem::getTaxCents)
                    .sum();

            taxReports.add(new com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.TaxReportItem(
                    rateName,
                    toBigDecimal(taxableAmountCents),
                    toBigDecimal(taxValCents)));
        });
        dto.setTaxReports(taxReports);

        // 7. POS Report
        com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.PosReportSection posReport = new com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.PosReportSection();
        posReport.setGrossSales(salesSection.getBillAmount()); // Or Subtotal + Tax? Bill Amount is Total.
        posReport.setDeduction(BigDecimal.ZERO); // Returns/Wastage could go here
        posReport.setNetSale(salesSection.getBillAmount());
        posReport.setTotal(salesSection.getBillAmount());
        dto.setPosReport(posReport);

        // 8. Cancellation Report
        // Currently we only have "cancelled" status sales.
        List<com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.CancellationReportItem> cancelledItems = cancelledSales
                .stream()
                .map(s -> new com.example.multi_tanent.pos.dto.report.DailySalesSummaryDto.CancellationReportItem(
                        s.getInvoiceNo(),
                        "Cancelled Sale", // or Customer Name
                        (long) s.getItems().size(),
                        toBigDecimal(s.getTotalCents())))
                .collect(Collectors.toList());
        dto.setCancellationReports(cancelledItems);

        return dto;
    }

    public List<com.example.multi_tanent.pos.dto.report.SalesByHourDto> getSalesByHour(LocalDate fromDate,
            LocalDate toDate) {
        Tenant tenant = getCurrentTenant();
        OffsetDateTime start = fromDate.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime end = toDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime()
                .minusNanos(1);

        List<Sale> allSales = saleRepository.findByTenantIdAndInvoiceDateBetween(tenant.getId(), start, end);

        // Filter valid sales
        List<Sale> validSales = allSales.stream()
                .filter(s -> !"cancelled".equalsIgnoreCase(s.getStatus()))
                .collect(Collectors.toList());

        // Initialize map for 24 hours
        Map<Integer, com.example.multi_tanent.pos.dto.report.SalesByHourDto> hourlyData = new java.util.HashMap<>();
        for (int i = 0; i < 24; i++) {
            String hourStr = String.format("%02d:00", i);
            hourlyData.put(i,
                    new com.example.multi_tanent.pos.dto.report.SalesByHourDto(hourStr, 0L, 0L, BigDecimal.ZERO));
        }

        // Aggregate
        for (Sale sale : validSales) {
            // Convert to system default zone to get local hour
            int hour = sale.getInvoiceDate().atZoneSameInstant(ZoneId.systemDefault()).getHour();
            com.example.multi_tanent.pos.dto.report.SalesByHourDto dto = hourlyData.get(hour);

            if (dto != null) {
                dto.setSalesCount(dto.getSalesCount() + 1);
                dto.setAmount(dto.getAmount().add(toBigDecimal(sale.getTotalCents())));

                long qty = sale.getItems().stream().mapToLong(com.example.multi_tanent.pos.entity.SaleItem::getQuantity)
                        .sum();
                dto.setQuantity(dto.getQuantity() + qty);
            }
        }

        return new ArrayList<>(hourlyData.values()).stream()
                .sorted(java.util.Comparator.comparing(com.example.multi_tanent.pos.dto.report.SalesByHourDto::getHour))
                .collect(Collectors.toList());
    }

    @Autowired
    private com.example.multi_tanent.pos.repository.CashRegisterRepository cashRegisterRepository;

    public List<com.example.multi_tanent.pos.dto.report.ClosingReportDto> getClosingReports(LocalDate fromDate,
            LocalDate toDate) {
        Tenant tenant = getCurrentTenant();
        OffsetDateTime start = fromDate.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime end = toDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime()
                .minusNanos(1);

        List<com.example.multi_tanent.pos.entity.CashRegister> registers = cashRegisterRepository
                .findByTenantIdAndOpeningTimeBetween(tenant.getId(), start, end);

        return registers.stream().map(cr -> {
            com.example.multi_tanent.pos.dto.report.ClosingReportDto dto = new com.example.multi_tanent.pos.dto.report.ClosingReportDto();
            dto.setId(cr.getId());
            dto.setOpeningDate(cr.getOpeningTime() != null ? cr.getOpeningTime().toString() : "");
            dto.setOpeningFloat(cr.getOpeningFloat());
            // Deriving "Running Date" from opening time
            dto.setRunningDate(cr.getOpeningTime() != null ? cr.getOpeningTime().toLocalDate().toString() : "");
            dto.setClosingDate(cr.getClosingTime() != null ? cr.getClosingTime().toString() : "");
            dto.setExpectedCashAmount(cr.getExpectedCashAmount());
            dto.setCountedCashAmount(cr.getCountedCashAmount());
            dto.setClosedCashDifference(cr.getClosedCashDifference());
            dto.setNotes(cr.getNotes());
            return dto;
        }).collect(Collectors.toList());
    }

}
