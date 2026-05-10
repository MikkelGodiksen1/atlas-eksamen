package dk.nap.atlas.model;

import java.time.LocalDateTime;

public class Mockup {
    private Long id;
    private Long customerId;
    private Long productId;
    private Long logoId;
    private String filePath;
    private String selectedSpecifications;
    private LocalDateTime generatedAt;

    public Mockup() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getLogoId() { return logoId; }
    public void setLogoId(Long logoId) { this.logoId = logoId; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getSelectedSpecifications() { return selectedSpecifications; }
    public void setSelectedSpecifications(String selectedSpecifications) { this.selectedSpecifications = selectedSpecifications; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
