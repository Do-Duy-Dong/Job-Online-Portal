package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.payload.Response.PendingEmployerResponse;
import com.example.demo.repository.EmployerRepository;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only endpoints (requires ROLE_ADMIN).
 * Secured globally via SecurityConfig: /api/admin/** → hasRole("ADMIN")
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthService authService;
    private final EmployerRepository employerRepository;

    /**
     * GET /api/admin/employers/pending
     * List all employer accounts waiting for admin approval.
     */
    @GetMapping("/employers/pending")
    public ResponseEntity<List<PendingEmployerResponse>> listPendingEmployers() {
        List<User> pendingUsers = authService.getPendingEmployers();

        List<PendingEmployerResponse> result = pendingUsers.stream()
                .map(u -> {
                    // Try to load Employer (company info) if available
                    var employerOpt = employerRepository.findByEmail(u.getEmail());

                    PendingEmployerResponse.PendingEmployerResponseBuilder builder =
                            PendingEmployerResponse.builder()
                                    .userId(u.getId())
                                    .fullName(u.getFullName())
                                    .email(u.getEmail())
                                    .approvalStatus(u.getApprovalStatus());

                    employerOpt.ifPresent(emp -> {
                        if (emp.getCompany() != null) {
                            builder.companyTitle(emp.getCompany().getTitle())
                                    .companyEmail(emp.getCompany().getEmail())
                                    .companyPhone(emp.getCompany().getPhone())
                                    .companyAddress(emp.getCompany().getAddress());
                        }
                    });

                    return builder.build();
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    /**
     * PUT /api/admin/employers/{userId}/approve
     * Approve an employer's registration — they can now login.
     */
    @PutMapping("/employers/{userId}/approve")
    public ResponseEntity<?> approveEmployer(@PathVariable String userId) {
        try {
            authService.approveEmployer(userId);
            return ResponseEntity.ok("Employer approved successfully");
        } catch (Exception e) {
            log.error("Error approving employer {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/admin/employers/{userId}/reject
     * Reject an employer's registration.
     */
    @PutMapping("/employers/{userId}/reject")
    public ResponseEntity<?> rejectEmployer(@PathVariable String userId) {
        try {
            authService.rejectEmployer(userId);
            return ResponseEntity.ok("Employer rejected");
        } catch (Exception e) {
            log.error("Error rejecting employer {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
