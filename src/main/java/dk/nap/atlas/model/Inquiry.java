package dk.nap.atlas.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Inquiry {
    private Long id;
    private Long customerId;
    private Long mockupId;
    private String inquiryType;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status;
    private String notes;
    private LocalDateTime createdAt;

    public Inquiry() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getMockupId() { return mockupId; }
    public void setMockupId(Long mockupId) { this.mockupId = mockupId; }
    public String getInquiryType() { return inquiryType; }
    public void setInquiryType(String inquiryType) { this.inquiryType = inquiryType; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
