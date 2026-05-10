package dk.nap.atlas.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FollowUp {
    private Long id;
    private Long customerId;
    private Long adminId;
    private LocalDate plannedDate;
    private String type;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public FollowUp() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public LocalDate getPlannedDate() { return plannedDate; }
    public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public boolean isOverdue() {
        return "planned".equals(status) && plannedDate != null && plannedDate.isBefore(LocalDate.now());
    }

    public boolean isToday() {
        return "planned".equals(status) && plannedDate != null && plannedDate.equals(LocalDate.now());
    }
}
