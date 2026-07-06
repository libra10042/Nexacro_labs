package com.nexacrolabs.excelupload.service;

import com.nexacrolabs.excelupload.domain.ExcelUploadRow;
import com.nexacrolabs.excelupload.repository.ExcelUploadRowRepository;
import com.nexacrolabs.excelupload.util.NexacroPlatformXmlBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelUploadService {

    private static final List<String> COLUMN_IDS = List.of("col_name", "col_age", "col_dept");
    private static final String DATASET_ID = "ds_list";

    private final ExcelUploadRowRepository excelUploadRowRepository;

    public ExcelUploadService(ExcelUploadRowRepository excelUploadRowRepository) {
        this.excelUploadRowRepository = excelUploadRowRepository;
    }

    /**
     * 엑셀 파일(1행: 헤더, 2행부터 데이터, A=이름 B=나이 C=부서 가정)을 읽어 DB에 저장하고,
     * 저장된 결과를 Nexacro Dataset 응답 XML 문자열로 변환한다.
     */
    @Transactional
    public String parseExcelAndSave(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return NexacroPlatformXmlBuilder.buildErrorResponse("업로드된 파일이 없습니다.");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !(filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls"))) {
            return NexacroPlatformXmlBuilder.buildErrorResponse("xls, xlsx 파일만 업로드할 수 있습니다.");
        }

        List<List<String>> rows;
        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            rows = new ArrayList<>();

            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                List<String> rowValues = new ArrayList<>();
                for (int colIdx = 0; colIdx < COLUMN_IDS.size(); colIdx++) {
                    rowValues.add(getCellValueAsString(row.getCell(colIdx)));
                }
                rows.add(rowValues);
            }
        } catch (IOException e) {
            return NexacroPlatformXmlBuilder.buildErrorResponse("엑셀 파일을 읽는 중 오류가 발생했습니다: " + e.getMessage());
        }

        if (rows.isEmpty()) {
            return NexacroPlatformXmlBuilder.buildErrorResponse("저장할 데이터가 없습니다. 엑셀 내용을 확인하세요.");
        }

        LocalDateTime uploadedAt = LocalDateTime.now();
        List<ExcelUploadRow> entities = rows.stream()
                .map(row -> new ExcelUploadRow(row.get(0), row.get(1), row.get(2), uploadedAt))
                .toList();

        excelUploadRowRepository.saveAll(entities);

        return NexacroPlatformXmlBuilder.buildDatasetResponse(DATASET_ID, COLUMN_IDS, rows);
    }

    /**
     * DB에 저장된 전체 데이터를 Nexacro Dataset 응답 XML로 조회한다.
     */
    @Transactional(readOnly = true)
    public String findAllAsPlatformXml() {
        List<List<String>> rows = excelUploadRowRepository.findAll().stream()
                .map(entity -> List.of(entity.getName(), entity.getAge(), entity.getDept()))
                .toList();

        return NexacroPlatformXmlBuilder.buildDatasetResponse(DATASET_ID, COLUMN_IDS, rows);
    }

    private boolean isRowEmpty(Row row) {
        for (int colIdx = 0; colIdx < COLUMN_IDS.size(); colIdx++) {
            if (!getCellValueAsString(row.getCell(colIdx)).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            double numericValue = cell.getNumericCellValue();
            if (numericValue == Math.floor(numericValue)) {
                return String.valueOf((long) numericValue);
            }
            return String.valueOf(numericValue);
        }

        return cell.toString().trim();
    }
}
