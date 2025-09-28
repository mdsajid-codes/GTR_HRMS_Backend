package com.example.multi_tanent.pos.controller;

import com.example.multi_tanent.pos.service.FileStorageService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/pos/uploads")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    public FileUploadController(@Qualifier("posFileStorageService") FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/product-image")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','POS_ADMIN', 'POS_MANAGER')")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        String fileUrl = fileStorageService.storeFile(file, "product-images");

        return ResponseEntity.ok(Map.of("imageUrl", fileUrl));
    }

    @PostMapping("/product-image/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','POS_ADMIN', 'POS_MANAGER')")
    public ResponseEntity<byte[]> uploadBulkProductImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            String errorContent = "Error: No files were selected for upload.";
            byte[] errorBytes = errorContent.getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "upload_error.txt");
            return ResponseEntity.badRequest().headers(headers).body(errorBytes);
        }

        // 1. Generate the text content (CSV format)
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("originalFilename,imageUrl\n"); // CSV Header

        Arrays.stream(files).forEach(file -> {
            String fileUrl = fileStorageService.storeFile(file, "product-images");
            csvContent.append(file.getOriginalFilename()).append(",").append(fileUrl).append("\n");
        });

        // 2. Convert the string to a byte array
        byte[] csvBytes = csvContent.toString().getBytes(StandardCharsets.UTF_8);

        // 3. Set headers for file download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "uploaded_image_urls.csv");

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }

    @GetMapping("/download-sample-text")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadSampleTextFile() {
        // 1. Generate your text content
        String content = "Hello, this is a sample text file.\n"
                       + "You can generate any text content you need here.\n"
                       + "Timestamp: " + java.time.LocalDateTime.now();

        // 2. Convert the string to a byte array
        byte[] textBytes = content.getBytes(StandardCharsets.UTF_8);

        // 3. Set headers for file download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "sample.txt");

        return ResponseEntity.ok().headers(headers).body(textBytes);
    }

    @GetMapping("/view/{tenantId}/{subfolder}/{filename:.+}")
    // Authorization is now handled by SecurityConfig to allow public access
    public ResponseEntity<Resource> viewFile(@PathVariable String tenantId, @PathVariable String subfolder, @PathVariable String filename, HttpServletRequest request) {
        // Construct the relative path from the URL parts
        String relativePath = Paths.get(tenantId, subfolder, filename).toString();

        Resource resource = fileStorageService.loadFileAsResource(relativePath);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // fallback to the default content type if type could not be determined
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"").body(resource);
    }
}