package com.example.demo.controller;

import com.example.demo.entity.CustomUserDetail;
import com.example.demo.entity.User;
import com.example.demo.payload.Request.ApplyRequest;
import com.example.demo.payload.Response.ApplyResponse;
import com.example.demo.payload.Response.ListJobApplication;
import com.example.demo.payload.Response.ResponsePageBase;
import com.example.demo.payload.Request.StatusApplyRequest;
import com.example.demo.service.JobApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
public class JobApplyController {
    private final JobApplicationService jobApplicationService;
    public JobApplyController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @PostMapping("/api/job-apply")
    public ResponseEntity<?> applyJob(
            @RequestBody ApplyRequest req,
            @AuthenticationPrincipal CustomUserDetail userDetail
            ){

            jobApplicationService.applyJob(
                    req.getJobId(),
                    req.getCvId(),
                    userDetail.getUsername()
            );
            return ResponseEntity.ok("Apply job successfully");

    }
    // GET /api/employee/applications - Xem danh sách job đã apply
    @GetMapping("/api/employee/applications")
    public ResponseEntity<?> getMyApplications(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            Authentication authentication
    ){
        User user = (User) authentication.getPrincipal();
        ResponsePageBase<ListJobApplication> response = jobApplicationService.getMyApplications(
                user.getId(),
                page,
                size
        );
        return ResponseEntity.ok(response);
    }

    // PATCH /api/employee/applications/{id}/cancel - Huỷ application
    @PatchMapping("/api/employee/applications/{id}/cancel")
    public ResponseEntity<?> cancelApplication(
            @PathVariable("id") String applicationId,
            Authentication authentication
    ){
        User user = (User) authentication.getPrincipal();
        jobApplicationService.cancelApplication(applicationId, user.getId());
        return ResponseEntity.ok("Application cancelled successfully");
    }
//    EMPLOYER
    @PreAuthorize("@employerCompanyValidator.isValidCompany(authentication, #id, 'JOB_READ')")
    @GetMapping("/api/employer/apply/view/{id}")
    public ResponseEntity<?> viewApplication(
            @PathVariable("id") String id,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "size",defaultValue = "10") Integer size,
            Authentication authentication
    ){
            CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
            ResponsePageBase<ApplyResponse> response= jobApplicationService.viewApplication(
                    id,
                    page,
                    size,
                    UUID.fromString(userDetail.getCompanyId())
            );
            return ResponseEntity.ok(response);
    }
    @PreAuthorize("@employerCompanyValidator.isValidCompany(authentication, #id, 'CV_STATUS_UPDATE')")
    @PatchMapping("/api/employer/apply/update")
    public ResponseEntity<?> updateApplicationStatus(
            @RequestBody StatusApplyRequest request
            ){

            jobApplicationService.updateStatusApplies(
                    request.getApplicationId(),
                    request.getStatus()
            );
            return ResponseEntity.ok("Update application status successfully");
    }



}
