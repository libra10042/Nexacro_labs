package com.nexacrolabs.upload.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    private String storageDir;
    private long maxFileSizeBytes;
    private List<String> allowedExtensions;
    private List<String> blockedExtensions;

    public String getStorageDir() {
        return storageDir;
    }

    public void setStorageDir(String storageDir) {
        this.storageDir = storageDir;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public List<String> getBlockedExtensions() {
        return blockedExtensions;
    }

    public void setBlockedExtensions(List<String> blockedExtensions) {
        this.blockedExtensions = blockedExtensions;
    }
}
