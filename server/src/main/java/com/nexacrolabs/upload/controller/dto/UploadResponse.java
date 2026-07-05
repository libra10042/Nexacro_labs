package com.nexacrolabs.upload.controller.dto;

import com.nexacrolabs.upload.domain.UploadFile;

public record UploadResponse(Long id, String originalName, long size, String contentType) {

    public static UploadResponse from(UploadFile file) {
        return new UploadResponse(file.getId(), file.getOriginalName(), file.getSize(), file.getContentType());
    }
}
