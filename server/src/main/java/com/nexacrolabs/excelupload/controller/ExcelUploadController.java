package com.nexacrolabs.excelupload.controller;

import com.nexacrolabs.excelupload.service.ExcelUploadService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/excel")
public class ExcelUploadController {

    private static final MediaType PLATFORM_XML = MediaType.valueOf("text/xml;charset=UTF-8");

    private final ExcelUploadService excelUploadService;

    public ExcelUploadController(ExcelUploadService excelUploadService) {
        this.excelUploadService = excelUploadService;
    }

    // Nexacro 클라이언트의 trans_upload.addParameter("uploadfile", path, "FILE") 과
    // 파라미터 이름("uploadfile")이 일치해야 함
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("uploadfile") MultipartFile uploadfile) {
        String responseXml = excelUploadService.parseExcelAndSave(uploadfile);

        return ResponseEntity.ok()
                .contentType(PLATFORM_XML)
                .body(responseXml);
    }

    // 업로드 후 DB에 저장된 내용을 다시 조회하고 싶을 때 사용 (예: 화면 새로고침)
    @GetMapping("/list")
    public ResponseEntity<String> list() {
        String responseXml = excelUploadService.findAllAsPlatformXml();

        return ResponseEntity.ok()
                .contentType(PLATFORM_XML)
                .body(responseXml);
    }
}
