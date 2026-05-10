package dk.nap.atlas.dto;

import jakarta.validation.constraints.*;

public class InquiryForm {

    @NotBlank(message = "Virksomhedsnavn er påkrævet")
    private String companyName;

    @NotBlank(message = "CVR er påkrævet")
    @Pattern(regexp = "\\d{8}", message = "CVR skal være 8 cifre")
    private String cvr;

    @NotBlank(message = "Kontaktperson er påkrævet")
    private String contactPerson;

    @NotBlank(message = "Email er påkrævet")
    @Email(message = "Email har ugyldigt format")
    private String email;

    @NotBlank(message = "Telefon er påkrævet")
    private String phone;

    private String address;

    @Min(value = 1, message = "Antal skal være mindst 1")
    private int quantity;

    private String message;

    private String preferredCallTime;

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String v) { this.companyName = v; }
    public String getCvr() { return cvr; }
    public void setCvr(String v) { this.cvr = v; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String v) { this.contactPerson = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int v) { this.quantity = v; }
    public String getMessage() { return message; }
    public void setMessage(String v) { this.message = v; }
    public String getPreferredCallTime() { return preferredCallTime; }
    public void setPreferredCallTime(String v) { this.preferredCallTime = v; }
}
