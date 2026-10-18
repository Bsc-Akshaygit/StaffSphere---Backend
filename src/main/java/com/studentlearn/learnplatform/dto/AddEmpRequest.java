package com.studentlearn.learnplatform.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class AddEmpRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    @Min(value = 1000000000L, message = "Phone number must be 10 digits")
    @Max(value = 9999999999L, message = "Phone number must be 10 digits")
    private long phone;

    @Min(value = 100000000000L, message = "Aadhaar must be 12 digits")
    @Max(value = 999999999999L, message = "Aadhaar must be 12 digits")
    private long aadhaar;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotNull(message = "Joining date is required")
    private LocalDate joingDate;

    @NotBlank(message = "Designation is required")
    private String designation;

    @Positive(message = "Salary must be a positive value")
    private float salary;

    @NotBlank(message = "Address is required")
    private String address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getPhone() {
        return phone;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }

    public long getAadhaar() {
        return aadhaar;
    }

    public void setAadhaar(long aadhaar) {
        this.aadhaar = aadhaar;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDate getJoingDate() {
        return joingDate;
    }

    public void setJoingDate(LocalDate joingDate) {
        this.joingDate = joingDate;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public float getSalary() {
        return salary;
    }

    public void setSalary(float salary) {
        this.salary = salary;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
