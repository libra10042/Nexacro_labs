package com.nexacrolabs.excelupload.controller;

import com.nexacrolabs.excelupload.service.ExcelUploadService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/excel")
public class ExcelUploadController {

    private final ExcelUploadService excelUploadService;

    public ExcelUploadController(ExcelUploadService excelUploadService) {
        this.excelUploadService = excelUploadService;
    }

    // Nexacro 클라이언트의 trans_upload.addParameter("uploadfile", path, "FILE") 과
    // 파라미터 이름("uploadfile")이 일치해야 함
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("uploadfile") MultipartFile uploadfile) {
        String responseXml = excelUploadService.parseExcelToPlatformXml(uploadfile);

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/xml;charset=UTF-8"))
                .body(responseXml);
    }
}
