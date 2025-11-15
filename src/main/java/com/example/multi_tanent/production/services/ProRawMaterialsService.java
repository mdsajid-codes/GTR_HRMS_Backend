package com.example.multi_tanent.production.services;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.production.dto.ProRawMaterialsRequest;
import com.example.multi_tanent.production.dto.ProRawMaterialsResponse;
import com.example.multi_tanent.production.entity.*;
import com.example.multi_tanent.production.enums.InventoryType;
import com.example.multi_tanent.production.enums.ItemType;
import com.example.multi_tanent.production.repository.*;
import com.example.multi_tanent.spersusers.enitity.Location;
import com.example.multi_tanent.tenant.service.FileStorageService;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional("tenantTx")
public class ProRawMaterialsService {

    private final ProRawMaterialsRepository rawMaterialsRepository;
    private final TenantRepository tenantRepository;
    private final LocationRepository locationRepository;
    private final ProCategoryRepository categoryRepository;
    private final ProSubCategoryRepository subCategoryRepository;
    private final ProUnitRepository unitRepository;
    private final ProTaxRepository taxRepository;
    private final BarcodeService barcodeService;
    private final FileStorageService fileStorageService;

    private Tenant getCurrentTenant() {
        String tenantId = TenantContext.getTenantId();
        return tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId));
    }

    public ProRawMaterialsResponse create(ProRawMaterialsRequest request) {
        Tenant tenant = getCurrentTenant();
        if (rawMaterialsRepository.existsByTenantIdAndItemCodeIgnoreCase(tenant.getId(), request.getItemCode())) {
            throw new IllegalArgumentException("Raw material with item code '" + request.getItemCode() + "' already exists.");
        }

        ProRawMaterials rawMaterial = new ProRawMaterials();
        mapRequestToEntity(request, rawMaterial, tenant);
        ProRawMaterials savedMaterial = rawMaterialsRepository.save(rawMaterial);
        return ProRawMaterialsResponse.fromEntity(updateBarcodeImage(savedMaterial));
    }

    @Transactional(readOnly = true)
    public Page<ProRawMaterialsResponse> getAll(Pageable pageable) {
        Long tenantId = getCurrentTenant().getId();
        return rawMaterialsRepository.findByTenantId(tenantId, pageable)
                .map(ProRawMaterialsResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProRawMaterialsResponse getById(Long id) {
        Long tenantId = getCurrentTenant().getId();
        return rawMaterialsRepository.findByTenantIdAndId(tenantId, id)
                .map(ProRawMaterialsResponse::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Raw material not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public ProRawMaterialsResponse getByItemCode(String itemCode) {
        Long tenantId = getCurrentTenant().getId();
        return rawMaterialsRepository.findByTenantIdAndItemCodeIgnoreCase(tenantId, itemCode)
                .map(ProRawMaterialsResponse::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Raw material not found with item code: " + itemCode));
    }


    public ProRawMaterialsResponse update(Long id, ProRawMaterialsRequest request) {
        Tenant tenant = getCurrentTenant();
        ProRawMaterials rawMaterial = rawMaterialsRepository.findByTenantIdAndId(tenant.getId(), id)
                .orElseThrow(() -> new EntityNotFoundException("Raw material not found with id: " + id));

        if (!rawMaterial.getItemCode().equalsIgnoreCase(request.getItemCode()) &&
            rawMaterialsRepository.existsByTenantIdAndItemCodeIgnoreCase(tenant.getId(), request.getItemCode())) {
            throw new IllegalArgumentException("Raw material with item code '" + request.getItemCode() + "' already exists.");
        }

        mapRequestToEntity(request, rawMaterial, tenant);
        ProRawMaterials savedMaterial = rawMaterialsRepository.save(rawMaterial);
        return ProRawMaterialsResponse.fromEntity(updateBarcodeImage(savedMaterial));
    }

    public void delete(Long id) {
        if (!rawMaterialsRepository.existsById(id)) {
            throw new EntityNotFoundException("Raw material not found with id: " + id);
        }
        rawMaterialsRepository.deleteById(id);
    }

    public List<String> bulkCreate(MultipartFile file) throws IOException {
        List<String> errors = new ArrayList<>();
        Tenant tenant = getCurrentTenant();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                try {
                    String itemCode = formatter.formatCellValue(row.getCell(0)).trim();
                    String name = formatter.formatCellValue(row.getCell(1)).trim();

                    if (itemCode.isEmpty() || name.isEmpty()) {
                        errors.add("Row " + (i + 1) + ": Item Code and Name are required.");
                        continue;
                    }

                    if (rawMaterialsRepository.existsByTenantIdAndItemCodeIgnoreCase(tenant.getId(), itemCode)) {
                        errors.add("Row " + (i + 1) + ": Raw material with item code '" + itemCode + "' already exists.");
                        continue;
                    }

                    ProRawMaterialsRequest request = new ProRawMaterialsRequest();
                    request.setItemCode(itemCode);
                    request.setName(name);

                    // Set optional fields
                    request.setBarcode(formatter.formatCellValue(row.getCell(2)));
                    request.setDescription(formatter.formatCellValue(row.getCell(3)));
                    request.setPurchasePrice(parseBigDecimal(formatter.formatCellValue(row.getCell(4))));
                    request.setSalesPrice(parseBigDecimal(formatter.formatCellValue(row.getCell(5))));
                    request.setReorderLimit(parseBigDecimal(formatter.formatCellValue(row.getCell(6))));
                    request.setUnitRelation(parseBigDecimal(formatter.formatCellValue(row.getCell(7))));
                    request.setCategoryId(parseLong(formatter.formatCellValue(row.getCell(8))));
                    request.setSubCategoryId(parseLong(formatter.formatCellValue(row.getCell(9))));
                    request.setIssueUnitId(parseLong(formatter.formatCellValue(row.getCell(10))));
                    request.setPurchaseUnitId(parseLong(formatter.formatCellValue(row.getCell(11))));
                    request.setTaxId(parseLong(formatter.formatCellValue(row.getCell(12))));
                    request.setLocationId(parseLong(formatter.formatCellValue(row.getCell(13))));
                    request.setInventoryType(parseEnum(InventoryType.class, formatter.formatCellValue(row.getCell(14)), InventoryType.RAW_MATERIAL));
                    request.setItemType(parseEnum(ItemType.class, formatter.formatCellValue(row.getCell(15)), ItemType.PRODUCT));

                    // Create and save the entity
                    ProRawMaterials rawMaterial = new ProRawMaterials();
                    mapRequestToEntity(request, rawMaterial, tenant);
                    ProRawMaterials savedMaterial = rawMaterialsRepository.save(rawMaterial);
                    updateBarcodeImage(savedMaterial); // Generate barcode for the newly created item

                } catch (Exception e) {
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                }
            }
        }
        return errors;
    }

    public ResponseEntity<byte[]> generateBulkUploadTemplate() throws IOException {
        String[] headers = {
                "Item Code*", "Name*", "Barcode", "Description", "Purchase Price", "Sales Price",
                "Reorder Limit", "Unit Relation", "Category ID", "Sub-Category ID",
                "Issue Unit ID", "Purchase Unit ID", "Tax ID", "Location ID",
                "Inventory Type (RAW_MATERIAL, FINISHED_GOOD, SERVICE, OTHER)",
                "Item Type (PRODUCT, SERVICE)"
        };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Raw Materials");

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
            responseHeaders.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            responseHeaders.setContentDispositionFormData("attachment", "raw_materials_bulk_upload_template.xlsx");

            return ResponseEntity.ok().headers(responseHeaders).body(out.toByteArray());
        }
    }

    public ResponseEntity<byte[]> exportAll() throws IOException {
        Long tenantId = getCurrentTenant().getId();
        List<ProRawMaterials> materials = rawMaterialsRepository.findByTenantId(tenantId);

        String[] headers = {
                "Item Code", "Name", "Barcode", "Description", "Purchase Price", "Sales Price",
                "Reorder Limit", "Unit Relation", "Category", "Sub-Category",
                "Issue Unit", "Purchase Unit", "Tax", "Location",
                "Inventory Type", "Item Type", "For Purchase", "For Sales", "Discontinued"
        };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Raw Materials");

            // Header row
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

            // Data rows
            int rowIdx = 1;
            for (ProRawMaterials material : materials) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(material.getItemCode());
                row.createCell(1).setCellValue(material.getName());
                row.createCell(2).setCellValue(material.getBarcode());
                row.createCell(3).setCellValue(material.getDescription());
                row.createCell(4).setCellValue(material.getPurchasePrice() != null ? material.getPurchasePrice().doubleValue() : 0.0);
                row.createCell(5).setCellValue(material.getSalesPrice() != null ? material.getSalesPrice().doubleValue() : 0.0);
                // ... and so on for all other fields
            }

            workbook.write(out);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            responseHeaders.setContentDispositionFormData("attachment", "raw_materials_export.xlsx");

            return ResponseEntity.ok().headers(responseHeaders).body(out.toByteArray());
        }
    }

    private void mapRequestToEntity(ProRawMaterialsRequest request, ProRawMaterials entity, Tenant tenant) {
        entity.setTenant(tenant);
        entity.setItemCode(request.getItemCode());
        entity.setName(request.getName());

        // Generate a random barcode if it's not provided
        if (request.getBarcode() == null || request.getBarcode().isBlank()) {
            entity.setBarcode(UUID.randomUUID().toString().substring(0, 13));
        } else {
            entity.setBarcode(request.getBarcode());
        }

        entity.setDescription(request.getDescription());
        entity.setInventoryType(request.getInventoryType());
        entity.setItemType(request.getItemType());
        entity.setForPurchase(request.isForPurchase());
        entity.setForSales(request.isForSales());
        entity.setUnitRelation(request.getUnitRelation());
        entity.setReorderLimit(request.getReorderLimit());
        entity.setPurchasePrice(request.getPurchasePrice());
        entity.setSalesPrice(request.getSalesPrice());
        entity.setDiscontinued(request.isDiscontinued());
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

    private ProRawMaterials updateBarcodeImage(ProRawMaterials material) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant ID is not set in the context.");
        }
        // The text encoded in the barcode will be the unique itemCode
        String barcodeText = material.getItemCode();
        byte[] barcodeImageBytes = barcodeService.generateBarcodeImage(barcodeText, 300, 100);

        // Define a unique filename for the barcode image
        String filename = "barcode-" + material.getItemCode().replaceAll("[^a-zA-Z0-9.-]", "_") + "-" + material.getId() + ".png";

        // If a barcode image already exists, delete the old one first
        if (material.getBarcodeImgUrl() != null && !material.getBarcodeImgUrl().isEmpty()) {
            fileStorageService.deleteFile(material.getBarcodeImgUrl());
        }

        String relativePath = fileStorageService.storeFile(barcodeImageBytes, "barcodes", filename);
        // The relative path from storeFile is already correct (e.g., "barcodes/file.png")
        // We just need to build the full URL from it.
        String fullUrl = buildFileUrl(relativePath);
        material.setBarcodeImgUrl(fullUrl);
        return rawMaterialsRepository.save(material);
    }

    private String buildFileUrl(String relativePath) {
        String tenantId = TenantContext.getTenantId();
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/") // This path must match MvcConfig
                .path(relativePath)
                .build()
                .toUriString();
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
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
            return defaultValue; // Or throw an error if strict validation is needed
        }
    }
}