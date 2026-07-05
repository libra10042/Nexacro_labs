package com.nexacrolabs.upload.service;

import com.nexacrolabs.upload.config.UploadProperties;
import com.nexacrolabs.upload.domain.UploadFile;
import com.nexacrolabs.upload.exception.InvalidFileException;
import com.nexacrolabs.upload.repository.UploadFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileStorageService {

    /** 확장자별 매직바이트 시그니처. 확장자 위장 업로드에 대한 2차 방어선. */
    private static final Map<String, byte[]> MAGIC_BYTES = Map.of(
            "jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
            "gif", new byte[]{0x47, 0x49, 0x46, 0x38},
            "pdf", new byte[]{0x25, 0x50, 0x44, 0x46}
    );

    private final UploadProperties properties;
    private final UploadFileRepository repository;
    private final Path storageDir;

    public FileStorageService(UploadProperties properties, UploadFileRepository repository) throws IOException {
        this.properties = properties;
        this.repository = repository;
        this.storageDir = Path.of(properties.getStorageDir()).toAbsolutePath().normalize();
        Files.createDirectories(storageDir);
    }

    public UploadFile store(MultipartFile multipartFile) {
        String originalName = sanitizeOriginalName(multipartFile.getOriginalFilename());
        String extension = extractExtension(originalName);

        validateExtension(extension);
        validateSize(multipartFile.getSize());

        String storedName = UUID.randomUUID() + "." + extension;
        Path targetPath = storageDir.resolve(storedName).normalize();

        // storageDir 하위가 아니면 거부 (경로 조작 방어, 정상 흐름에서는 발생하지 않음)
        if (!targetPath.getParent().equals(storageDir)) {
            throw new InvalidFileException("잘못된 저장 경로입니다.");
        }

        try (InputStream in = multipartFile.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new InvalidFileException("파일 저장에 실패했습니다.");
        }

        validateMagicBytes(targetPath, extension);

        try {
            UploadFile entity = new UploadFile(originalName, storedName, multipartFile.getSize(),
                    multipartFile.getContentType());
            return repository.save(entity);
        } catch (RuntimeException e) {
            deleteQuietly(targetPath);
            throw e;
        }
    }

    public Optional<UploadFile> find(Long id) {
        return repository.findById(id);
    }

    public Path resolveStoredPath(UploadFile file) {
        return storageDir.resolve(file.getStoredName()).normalize();
    }

    private String sanitizeOriginalName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            throw new InvalidFileException("파일명이 비어 있습니다.");
        }
        // 브라우저/OS별로 경로 전체가 오는 경우가 있어 파일명만 추출.
        // Path.of()는 JVM 네이티브 인코딩에 따라 한글 등 유니코드 파일명에서
        // InvalidPathException을 던질 수 있어 문자열 연산으로만 처리한다.
        String name = originalName.replace('\\', '/');
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }
        if (name.isBlank() || name.equals(".") || name.equals("..")) {
            throw new InvalidFileException("허용되지 않는 파일명입니다.");
        }
        return name;
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new InvalidFileException("확장자가 없는 파일은 업로드할 수 없습니다.");
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    private void validateExtension(String extension) {
        if (properties.getBlockedExtensions().contains(extension)) {
            throw new InvalidFileException("허용되지 않는 파일 형식입니다: " + extension);
        }
        if (!properties.getAllowedExtensions().contains(extension)) {
            throw new InvalidFileException("허용되지 않는 파일 형식입니다: " + extension);
        }
    }

    private void validateSize(long size) {
        if (size <= 0) {
            throw new InvalidFileException("빈 파일은 업로드할 수 없습니다.");
        }
        if (size > properties.getMaxFileSizeBytes()) {
            throw new InvalidFileException("파일 용량이 제한을 초과했습니다.");
        }
    }

    private void validateMagicBytes(Path path, String extension) {
        byte[] signature = MAGIC_BYTES.get(extension);
        if (signature == null) {
            return; // 시그니처를 정의하지 않은 확장자(office 문서, zip 등)는 건너뜀
        }
        try {
            byte[] header = new byte[signature.length];
            try (InputStream in = Files.newInputStream(path)) {
                int read = in.readNBytes(header, 0, header.length);
                if (read < header.length) {
                    throw new InvalidFileException("파일 내용이 손상되었거나 형식이 올바르지 않습니다.");
                }
            }
            for (int i = 0; i < signature.length; i++) {
                if (header[i] != signature[i]) {
                    deleteQuietly(path);
                    throw new InvalidFileException("확장자와 실제 파일 형식이 일치하지 않습니다.");
                }
            }
        } catch (IOException e) {
            deleteQuietly(path);
            throw new InvalidFileException("파일 검증 중 오류가 발생했습니다.");
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // 정리 실패는 로깅만 하고 원래 예외를 그대로 전파
        }
    }
}
