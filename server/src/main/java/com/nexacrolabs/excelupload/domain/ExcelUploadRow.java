package com.nexacrolabs.excelupload.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "excel_upload_row")
public class ExcelUploadRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 256)
    private String name;

    @Column(name = "age", length = 256)
    private String age;

    @Column(name = "dept", length = 256)
    private String dept;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    protected ExcelUploadRow() {
        // JPA
    }

    public ExcelUploadRow(String name, String age, String dept, LocalDateTime uploadedAt) {
        this.name = name;
        this.age = age;
        this.dept = dept;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getDept() {
        return dept;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
}
