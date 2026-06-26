package com.blogmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be a valid email address")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}
