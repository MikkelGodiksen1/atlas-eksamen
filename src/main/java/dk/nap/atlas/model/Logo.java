package dk.nap.atlas.model;

import java.time.LocalDateTime;

public class Logo {
    private Long id;
    private Long customerId;
    private String filename;
    private String filePath;
    private long fileSizeBytes;
    private String mimeType;
    private LocalDateTime uploadedAt;

    public Logo() {}

    public Logo(String filename, String filePath, long fileSizeBytes, String mimeType) {
        this.filename = filename;
        this.filePath = filePath;
        this.fileSizeBytes = fileSizeBytes;
        this.mimeType = mimeType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
