package com.example.demo.controller.Employer;

import com.example.demo.entity.CustomUserDetail;
import com.example.demo.payload.Request.CreateEmployerByManagerRequest;
import com.example.demo.payload.Request.CustomStaffPermissionRequest;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Authentication;
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
    @PreAuthorize("@employerJobValidator.canAccessJob(authentication, null, 'EDIT_ROLE')")
    @PostMapping("/editRole")
    public ResponseEntity<?> editEmployerRole(
            Authentication authentication,
            @RequestBody CustomStaffPermissionRequest request
            ) {
        try {
            authService.customPermissionForStaff(request);
            return ResponseEntity.ok("Employer role updated successfully");
        } catch (Exception e) {
            log.error("Error updating employer role: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("@employerJobValidator.canAccessJob(authentication, null, 'VIEW_ROLE')")
    @GetMapping("/permissions")
    public ResponseEntity<?> getPermissionsByCompany(
        @RequestParam String companyId,
        Authentication authentication) {
        try {
            return ResponseEntity.ok(authService.getPermissioByCoompanyId(companyId));
        } catch (Exception e) {
            log.error("Error getting permissions: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
