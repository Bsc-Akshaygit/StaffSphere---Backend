package com.studentlearn.learnplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Email Must be Non Empty")
    @Email(message = "Enter a Valid Email")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 4,max = 8, message = "Password must contain at least 8 characters")
    private String password;
    public LoginRequest() {
    }
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
