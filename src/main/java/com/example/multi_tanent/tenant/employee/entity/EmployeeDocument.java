package com.example.multi_tanent.tenant.employee.entity;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table (name = "employee_document")
@Getter
@Setter
@ToString(exclude = "employee")
@EqualsAndHashCode(exclude = "employee")
public class EmployeeDocument {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "employee_id")
    @JsonBackReference
    private Employee employee;

    @Column(name = "doc_type", nullable = false, length = 50)
    private String docType;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_path", length = 2048)
    private String filePath;

    @Column(name = "preview_path", length = 2048)
    private String previewPath;

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @Column(length = 2000)
    private String remarks;
}
