package com.nexacrolabs.excelupload.repository;

import com.nexacrolabs.excelupload.domain.ExcelUploadRow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExcelUploadRowRepository extends JpaRepository<ExcelUploadRow, Long> {
}
