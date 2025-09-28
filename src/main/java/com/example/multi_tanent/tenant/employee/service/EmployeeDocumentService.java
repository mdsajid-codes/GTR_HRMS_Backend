package com.example.multi_tanent.tenant.employee.service;

import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.tenant.employee.entity.EmployeeDocument;
import com.example.multi_tanent.tenant.employee.repository.EmployeeDocumentRepository;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(transactionManager = "tenantTx")
public class EmployeeDocumentService {

    private final EmployeeDocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;
    private final FileStorageService fileStorageService;

    public EmployeeDocumentService(EmployeeDocumentRepository documentRepository,
                                   EmployeeRepository employeeRepository,
                                   FileStorageService fileStorageService) {
        this.documentRepository = documentRepository;
        this.employeeRepository = employeeRepository;
        this.fileStorageService = fileStorageService;
    }

    public EmployeeDocument storeDocument(MultipartFile file, String employeeCode, String docType, String remarks) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + employeeCode));

        String fileName = fileStorageService.storeFile(file, employeeCode);

        EmployeeDocument doc = new EmployeeDocument();
        doc.setEmployee(employee);
        doc.setDocType(docType);
        doc.setFileName(fileName);
        doc.setFilePath(fileStorageService.getFileStorageLocation().resolve(fileName).toString());
        doc.setRemarks(remarks);
        doc.setVerified(false);

        return documentRepository.save(doc);
    }

    @Transactional(readOnly = true)
    public Optional<EmployeeDocument> getDocument(Long fileId) {
        return documentRepository.findById(fileId);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDocument> getDocumentsForEmployee(String employeeCode) {
        employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + employeeCode));
        return documentRepository.findByEmployeeEmployeeCode(employeeCode);
    }

    public Resource loadFile(String fileName) {
        return fileStorageService.loadFileAsResource(fileName);
    }

    public void deleteDocument(Long documentId) {
        EmployeeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
        
        fileStorageService.deleteFile(doc.getFileName());
        documentRepository.delete(doc);
    }
    
    public EmployeeDocument updateDocumentDetails(Long documentId, String docType, String remarks, Boolean verified) {
        EmployeeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
        
        doc.setDocType(docType);
        doc.setRemarks(remarks);
        doc.setVerified(verified);
        
        return documentRepository.save(doc);
    }
}