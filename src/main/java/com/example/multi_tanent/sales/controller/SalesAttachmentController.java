package com.example.multi_tanent.sales.controller;

import com.example.multi_tanent.tenant.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/sales/attachments/quotations")
@RequiredArgsConstructor
public class SalesAttachmentController {

    private final FileStorageService fileStorageService;

    @GetMapping("/**")
    public ResponseEntity<Resource> getAttachment(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // Extract path after /api/sales/attachments/
        String filePath = requestUri.substring(requestUri.indexOf("/attachments/") + "/attachments/".length());
        // URL decode the file path to handle spaces and special characters
        filePath = java.net.URLDecoder.decode(filePath, java.nio.charset.StandardCharsets.UTF_8);

        Resource resource = fileStorageService.loadFileAsResource(filePath, true);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // fallback
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
