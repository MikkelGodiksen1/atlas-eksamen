package dk.nap.atlas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class FollowUpForm {
    @NotNull private Long customerId;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate plannedDate;
    @NotBlank private String type;
    private String notes;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long v) { this.customerId = v; }
    public LocalDate getPlannedDate() { return plannedDate; }
    public void setPlannedDate(LocalDate v) { this.plannedDate = v; }
    public String getType() { return type; }
    public void setType(String v) { this.type = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { this.notes = v; }
}
