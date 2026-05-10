package dk.nap.atlas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CustomerForm {
    @NotBlank private String companyName;
    @NotBlank @Pattern(regexp = "\\d{8}", message = "CVR skal være 8 cifre") private String cvr;
    @NotBlank private String contactPerson;
    @NotBlank @Email private String email;
    @NotBlank private String phone;
    private String address;

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
}
