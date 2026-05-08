package com.example.demo.controller.Employer;

import com.example.demo.payload.Request.CreateEmployerByManagerRequest;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Employer management endpoints accessible only by MANAGER_HR role.
 * MANAGER_HR can create new employer accounts directly (pre-approved).
 *
 * Accessible via /api/employer/** which already requires ROLE_EMPLOYER (see SecurityConfig).
 * Additional role guard via @PreAuthorize.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employer/management")
public class EmployerManagementController {

    private final AuthService authService;

    /**
     * POST /api/employer/management/create
     * Only MANAGER_HR can call this endpoint.
     * Creates a new employer account with the specified role — immediately APPROVED.
     *
     * Request body:
     * {
     *   "fullName": "Nguyen Van A",
     *   "email": "nva@company.com",
     *   "password": "secret123",
     *   "roleName": "HR",          // optional, defaults to EMPLOYER
     *   "companyTitle": "Acme Corp",
     *   "companyAddress": "123 Main St",
     *   "companyPhone": "0901234567",
     *   "companyEmail": "hr@acme.com"
     * }
     */
    @PreAuthorize("hasAuthority('MANAGER_HR')")
    @PostMapping("/create")
    public ResponseEntity<?> createEmployer(
            @RequestBody CreateEmployerByManagerRequest request) {
        try {
            authService.createEmployerByManager(request);
            return ResponseEntity.ok("Employer created successfully");
        } catch (Exception e) {
            log.error("Error creating employer: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
