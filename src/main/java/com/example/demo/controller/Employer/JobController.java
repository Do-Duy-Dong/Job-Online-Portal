package com.example.demo.controller.Employer;

import com.example.demo.entity.CustomUserDetail;
import com.example.demo.payload.Request.JobRequest;
import com.example.demo.payload.Response.JobAllResponse;
import com.example.demo.payload.Response.KeysetPageResponse;
import com.example.demo.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Controller
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PreAuthorize("hasAnyAuthority('PERMISSION_JOB_CREATE_ALL', 'PERMISSION_JOB_CREATE_OWN')")
    @PostMapping("api/employer/job/create")
    public ResponseEntity<?> createJob(
            @RequestBody JobRequest jobRequest,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            log.info("job: {}", jobRequest.getExpirationDate());
            jobService.createJob(jobRequest, email);
            return ResponseEntity.ok("Job created successfully");
        } catch (Exception e) {
            log.error("Error creating job: {}", e);
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    /**
     * PUT /api/employer/job/update/{id}
     * Requires: EMPLOYER role + JOB_UPDATE permission
     * Additional: employer must own the job (checked in service)
     */
    @PreAuthorize("@employerJobValidator.canAccessJob(authentication, #id, 'JOB_UPDATE')")
    @PutMapping("/api/employer/job/update/{id}")
    public ResponseEntity<?> updateJob(
            @PathVariable String id,
            @RequestBody JobRequest jobRequest,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            jobService.updateJob(UUID.fromString(id), jobRequest, email);
            return ResponseEntity.ok("Job updated successfully");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating job: {}", e.getMessage());
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    /**
     * DELETE /api/employer/job/delete/{id}
     * Requires: EMPLOYER role + JOB_DELETE permission
     * Additional: employer must own the job (checked in service)
     */
    @PreAuthorize("@employerJobValidator.canAccessJob(authentication, #id, 'JOB_DELETE')")
    @DeleteMapping("/api/employer/job/delete/{id}")
    public ResponseEntity<?> deleteJob(
            @PathVariable String id,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            jobService.deleteJob(UUID.fromString(id), email);
            return ResponseEntity.ok("Job deleted (closed) successfully");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting job: {}", e.getMessage());
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    /**
     * GET /api/company/{companyId}/jobs
     * Requires: EMPLOYER role + JOB_READ permission
     * Additional: employer's companyId in JWT must match the {companyId} in path
     */
    @GetMapping("/api/company/{companyId}/jobs")
    public ResponseEntity<?> getJobsByCompany(
            @PathVariable String companyId,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "cursorTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorTime,
            @RequestParam(value = "cursorId", required = false) String cursorId,
            Authentication authentication) {
        try {
            CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
            String requesterCompanyId = userDetail.getCompanyId();

            UUID cursorUuid = (cursorId != null && !cursorId.isBlank()) ? UUID.fromString(cursorId) : null;

            KeysetPageResponse<JobAllResponse> jobs = jobService.getJobsByCompany(
                    UUID.fromString(companyId),
                    requesterCompanyId,
                    size,
                    cursorTime,
                    cursorUuid);
            return ResponseEntity.ok(jobs);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching company jobs: {}", e.getMessage());
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}
