package com.studentlearn.learnplatform.dto;

import java.time.LocalDate;

public class ViewEmployee {

    private String name;
    private String email;
    private long phone;
    private long aadhaar;
    private LocalDate dateOfBirth;
    private LocalDate joiningDate;
    private String designation;
    private float salary;
    private String address;

    public ViewEmployee(
            String name,
            String email,
            long phone,
            long aadhaar,
            LocalDate dateOfBirth,
            LocalDate joiningDate,
            String designation,
            float salary,
            String address) {

        this.name = name;
        this.email = email;
        this.phone = phone;
        this.aadhaar = aadhaar;
        this.dateOfBirth = dateOfBirth;
        this.joiningDate = joiningDate;
        this.designation = designation;
        this.salary = salary;
        this.address = address;
    }

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

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
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

