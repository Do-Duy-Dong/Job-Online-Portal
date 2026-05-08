package com.example.demo.payload.Request;

import lombok.Getter;
import lombok.Setter;

/**
 * Extended request for MANAGER_HR to create a new employer.
 * Inherits all RegisterEmployerRequest fields + adds roleName.
 * Note: confirmPassword is not required when admin/manager creates the account.
 */
@Getter
@Setter
public class CreateEmployerByManagerRequest {
    private String companyTitle;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;
    private String fullName;
    private String email;
    private String password;
    /** Role to assign to the new employer (e.g. HR, MANAGER_HR, HR_INTERN, ASSISTANT) */
    private String roleName;
}
