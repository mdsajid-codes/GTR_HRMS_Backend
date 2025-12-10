package com.example.multi_tanent.production.services;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.production.dto.ProSemiFinishedRequest;
import com.example.multi_tanent.production.dto.ProSemiFinishedResponse;
import com.example.multi_tanent.production.entity.*;
import com.example.multi_tanent.production.enums.InventoryType;
import com.example.multi_tanent.production.enums.ItemType;
import com.example.multi_tanent.production.repository.*;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import com.example.multi_tanent.spersusers.repository.LocationRepository;
import com.example.multi_tanent.spersusers.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional("tenantTx")
public class ProSemiFinishedService {

    private final ProSemiFinishedRepository semiFinishedRepository;
    private final TenantRepository tenantRepository;
    private final LocationRepository locationRepository;
    private final ProCategoryRepository categoryRepository;
    private final ProSubCategoryRepository subCategoryRepository;
    private final ProUnitRepository unitRepository;
    private final ProTaxRepository taxRepository;

    private Tenant getCurrentTenant() {
        String tenantId = TenantContext.getTenantId();
        return tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId));
    }

    public ProSemiFinishedResponse create(ProSemiFinishedRequest request) {
        Tenant tenant = getCurrentTenant();
        if (semiFinishedRepository.existsByTenantIdAndItemCodeIgnoreCase(tenant.getId(), request.getItemCode())) {
            throw new IllegalArgumentException(
                    "Semi-finished good with item code '" + request.getItemCode() + "' already exists.");
        }

        ProSemifinished semiFinished = new ProSemifinished();
        mapRequestToEntity(request, semiFinished, tenant);
        ProSemifinished savedEntity = semiFinishedRepository.save(semiFinished);
        return ProSemiFinishedResponse.fromEntity(savedEntity);
    }

    @Transactional(readOnly = true)
    public Page<ProSemiFinishedResponse> getAll(Pageable pageable) {
        Long tenantId = getCurrentTenant().getId();
        return semiFinishedRepository.findByTenantId(tenantId, pageable)
                .map(ProSemiFinishedResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProSemiFinishedResponse getById(Long id) {
        Long tenantId = getCurrentTenant().getId();
        return semiFinishedRepository.findByTenantIdAndId(tenantId, id)
                .map(ProSemiFinishedResponse::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Semi-finished good not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public ProSemiFinishedResponse getByItemCode(String itemCode) {
        Long tenantId = getCurrentTenant().getId();
        return semiFinishedRepository.findByTenantIdAndItemCodeIgnoreCase(tenantId, itemCode)
                .map(ProSemiFinishedResponse::fromEntity)
                .orElseThrow(
                        () -> new EntityNotFoundException("Semi-finished good not found with item code: " + itemCode));
    }

    public ProSemiFinishedResponse update(Long id, ProSemiFinishedRequest request) {
        Tenant tenant = getCurrentTenant();
        ProSemifinished semiFinished = semiFinishedRepository.findByTenantIdAndId(tenant.getId(), id)
                .orElseThrow(() -> new EntityNotFoundException("Semi-finished good not found with id: " + id));

        if (!semiFinished.getItemCode().equalsIgnoreCase(request.getItemCode()) &&
                semiFinishedRepository.existsByTenantIdAndItemCodeIgnoreCase(tenant.getId(), request.getItemCode())) {
            throw new IllegalArgumentException(
                    "Semi-finished good with item code '" + request.getItemCode() + "' already exists.");
        }

        mapRequestToEntity(request, semiFinished, tenant);
        ProSemifinished savedEntity = semiFinishedRepository.save(semiFinished);
        return ProSemiFinishedResponse.fromEntity(savedEntity);
    }

    public void delete(Long id) {
        if (!semiFinishedRepository.existsById(id)) {
            throw new EntityNotFoundException("Semi-finished good not found with id: " + id);
        }
        semiFinishedRepository.deleteById(id);
    }

    public List<String> bulkCreate(MultipartFile file) throws IOException {
        List<String> errors = new ArrayList<>();
        Tenant tenant = getCurrentTenant();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row))
                    continue;

                try {
                    String itemCode = formatter.formatCellValue(row.getCell(0)).trim();
                    String name = formatter.formatCellValue(row.getCell(1)).trim();

                    if (itemCode.isEmpty() || name.isEmpty()) {
                        errors.add("Row " + (i + 1) + ": Item Code and Name are required.");
                        continue;
                    }

                    if (semiFinishedRepository.existsByTenantIdAndItemCodeIgnoreCase(tenant.getId(), itemCode)) {
                        errors.add("Row " + (i + 1) + ": Item with code '" + itemCode + "' already exists.");
                        continue;
                    }

                    ProSemiFinishedRequest request = new ProSemiFinishedRequest();
                    request.setItemCode(itemCode);
                    request.setName(name);
                    request.setDescription(formatter.formatCellValue(row.getCell(2)));
                    request.setInventoryType(parseEnum(InventoryType.class, formatter.formatCellValue(row.getCell(3)),
                            InventoryType.SEMI_FINISHED_GOOD));
                    request.setItemType(
                            parseEnum(ItemType.class, formatter.formatCellValue(row.getCell(4)), ItemType.PRODUCT));
                    request.setForPurchase(parseBoolean(formatter.formatCellValue(row.getCell(5))));
                    request.setForSales(parseBoolean(formatter.formatCellValue(row.getCell(6))));
                    request.setRoll(parseBoolean(formatter.formatCellValue(row.getCell(7))));
                    request.setScrapItem(parseBoolean(formatter.formatCellValue(row.getCell(8))));
                    request.setUnitRelation(parseBigDecimal(formatter.formatCellValue(row.getCell(9))));
                    request.setWastagePercentage(parseBigDecimal(formatter.formatCellValue(row.getCell(10))));
                    request.setReorderLimit(parseBigDecimal(formatter.formatCellValue(row.getCell(11))));
                    request.setPurchasePrice(parseBigDecimal(formatter.formatCellValue(row.getCell(12))));
                    request.setSalesPrice(parseBigDecimal(formatter.formatCellValue(row.getCell(13))));
                    request.setTaxInclusive(parseBoolean(formatter.formatCellValue(row.getCell(14))));

                    request.setCategoryId(parseLong(formatter.formatCellValue(row.getCell(15))));
                    request.setSubCategoryId(parseLong(formatter.formatCellValue(row.getCell(16))));
                    request.setIssueUnitId(parseLong(formatter.formatCellValue(row.getCell(17))));
                    request.setPurchaseUnitId(parseLong(formatter.formatCellValue(row.getCell(18))));
                    request.setTaxId(parseLong(formatter.formatCellValue(row.getCell(19))));
                    request.setLocationId(parseLong(formatter.formatCellValue(row.getCell(20))));

                    ProSemifinished entity = new ProSemifinished();
                    mapRequestToEntity(request, entity, tenant);
                    semiFinishedRepository.save(entity);

                } catch (Exception e) {
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                }
            }
        }
        return errors;
    }

    public ResponseEntity<byte[]> generateBulkUploadTemplate() throws IOException {
        String[] headers = {
                "Item Code*", "Name*", "Description",
                "Inventory Type (SEMI_FINISHED_GOOD)", "Item Type (PRODUCT, SERVICE)",
                "For Purchase (true/false)", "For Sales (true/false)",
                "Is Roll (true/false)", "Is Scrap Item (true/false)",
                "Unit Relation", "Wastage %", "Reorder Limit",
                "Purchase Price", "Sales Price", "Tax Inclusive (true/false)",
                "Category ID", "Sub-Category ID", "Issue Unit ID", "Purchase Unit ID", "Tax ID", "Location ID"
        };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Semi-Finished Goods");
            Row headerRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerCellStyle.setFont(font);

            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
                sheet.autoSizeColumn(col);
            }

            workbook.write(out);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            responseHeaders.setContentDispositionFormData("attachment", "semi_finished_bulk_upload_template.xlsx");
            return ResponseEntity.ok().headers(responseHeaders).body(out.toByteArray());
        }
    }

    public ResponseEntity<byte[]> exportAll() throws IOException {
        Long tenantId = getCurrentTenant().getId();
        List<ProSemifinished> items = semiFinishedRepository.findByTenantId(tenantId);

        String[] headers = {
                "Item Code", "Name", "Description", "Inventory Type", "Item Type",
                "For Purchase", "For Sales", "Is Roll", "Is Scrap Item",
                "Unit Relation", "Wastage %", "Reorder Limit",
                "Purchase Price", "Sales Price", "Tax Inclusive",
                "Category", "Sub-Category", "Issue Unit", "Purchase Unit", "Tax", "Location"
        };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Semi-Finished Goods");
            Row headerRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerCellStyle.setFont(font);

            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (ProSemifinished item : items) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;
                row.createCell(col++).setCellValue(item.getItemCode());
                row.createCell(col++).setCellValue(item.getName());
                row.createCell(col++).setCellValue(item.getDescription());
                row.createCell(col++)
                        .setCellValue(item.getInventoryType() != null ? item.getInventoryType().name() : "");
                row.createCell(col++).setCellValue(item.getItemType() != null ? item.getItemType().name() : "");
                row.createCell(col++).setCellValue(item.isForPurchase());
                row.createCell(col++).setCellValue(item.isForSales());
                row.createCell(col++).setCellValue(item.isRoll());
                row.createCell(col++).setCellValue(item.isScrapItem());
                row.createCell(col++)
                        .setCellValue(item.getUnitRelation() != null ? item.getUnitRelation().doubleValue() : 0.0);
                row.createCell(col++).setCellValue(
                        item.getWastagePercentage() != null ? item.getWastagePercentage().doubleValue() : 0.0);
                row.createCell(col++)
                        .setCellValue(item.getReorderLimit() != null ? item.getReorderLimit().doubleValue() : 0.0);
                row.createCell(col++)
                        .setCellValue(item.getPurchasePrice() != null ? item.getPurchasePrice().doubleValue() : 0.0);
                row.createCell(col++)
                        .setCellValue(item.getSalesPrice() != null ? item.getSalesPrice().doubleValue() : 0.0);
                row.createCell(col++).setCellValue(item.isTaxInclusive());
                row.createCell(col++).setCellValue(item.getCategory() != null ? item.getCategory().getName() : "");
                row.createCell(col++)
                        .setCellValue(item.getSubCategory() != null ? item.getSubCategory().getName() : "");
                row.createCell(col++).setCellValue(item.getIssueUnit() != null ? item.getIssueUnit().getName() : "");
                row.createCell(col++)
                        .setCellValue(item.getPurchaseUnit() != null ? item.getPurchaseUnit().getName() : "");
                row.createCell(col++).setCellValue(item.getTax() != null ? item.getTax().getCode() : "");
                row.createCell(col++).setCellValue(item.getLocation() != null ? item.getLocation().getName() : "");
            }

            workbook.write(out);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            responseHeaders.setContentDispositionFormData("attachment", "semi_finished_export.xlsx");
            return ResponseEntity.ok().headers(responseHeaders).body(out.toByteArray());
        }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null)
            return true;
        DataFormatter formatter = new DataFormatter();
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && !formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private Long parseLong(String value) {
        return (value == null || value.isBlank()) ? null : Long.parseLong(value);
    }

    private BigDecimal parseBigDecimal(String value) {
        return (value == null || value.isBlank()) ? null : new BigDecimal(value);
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value, T defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    private boolean parseBoolean(String value) {
        return Boolean.parseBoolean(value);
    }

    private void mapRequestToEntity(ProSemiFinishedRequest request, ProSemifinished entity, Tenant tenant) {
        entity.setTenant(tenant);
        entity.setItemCode(request.getItemCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setInventoryType(
                request.getInventoryType() != null ? request.getInventoryType() : InventoryType.SEMI_FINISHED_GOOD);
        entity.setItemType(request.getItemType() != null ? request.getItemType() : ItemType.PRODUCT);
        entity.setForPurchase(request.isForPurchase());
        entity.setForSales(request.isForSales());
        entity.setRoll(request.isRoll());
        entity.setScrapItem(request.isScrapItem());
        entity.setUnitRelation(request.getUnitRelation());
        entity.setWastagePercentage(request.getWastagePercentage());
        entity.setReorderLimit(request.getReorderLimit());
        entity.setPurchasePrice(request.getPurchasePrice());
        entity.setSalesPrice(request.getSalesPrice());
        entity.setTaxInclusive(request.isTaxInclusive());
        entity.setPicturePath(request.getPicturePath());

        if (request.getLocationId() != null) {
            entity.setLocation(locationRepository.findById(request.getLocationId()).orElse(null));
        } else {
            entity.setLocation(null);
        }

        if (request.getCategoryId() != null) {
            entity.setCategory(categoryRepository.findById(request.getCategoryId()).orElse(null));
        } else {
            entity.setCategory(null);
        }

        if (request.getSubCategoryId() != null) {
            entity.setSubCategory(subCategoryRepository.findById(request.getSubCategoryId()).orElse(null));
        } else {
            entity.setSubCategory(null);
        }

        if (request.getIssueUnitId() != null) {
            entity.setIssueUnit(unitRepository.findById(request.getIssueUnitId()).orElse(null));
        } else {
            entity.setIssueUnit(null);
        }

        if (request.getPurchaseUnitId() != null) {
            entity.setPurchaseUnit(unitRepository.findById(request.getPurchaseUnitId()).orElse(null));
        } else {
            entity.setPurchaseUnit(null);
        }

        if (request.getTaxId() != null) {
            entity.setTax(taxRepository.findById(request.getTaxId()).orElse(null));
        } else {
            entity.setTax(null);
        }
    }
}
