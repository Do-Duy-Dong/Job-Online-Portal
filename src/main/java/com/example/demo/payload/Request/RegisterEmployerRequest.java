package com.example.demo.payload.Request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterEmployerRequest {
    private String companyTitle;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;
    private String fullName;
    private String email;
    private String password;
    private String confirmPassword;
}
