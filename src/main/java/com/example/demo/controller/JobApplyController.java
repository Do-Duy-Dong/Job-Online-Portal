package com.example.demo.controller;

import com.example.demo.payload.Response.ApplyResponse;
import com.example.demo.payload.Response.ResponsePageBase;
import com.example.demo.payload.Request.StatusApplyRequest;
import com.example.demo.service.JobApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Controller
public class JobApplyController {
    private final JobApplicationService jobApplicationService;
    public JobApplyController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @PostMapping("/api/job-apply")
    public ResponseEntity<?> applyJob(
            @RequestBody Map<String,String > req
            ){

            String jobId= req.get("jobId");
            String cvId= req.get("cvId");
            jobApplicationService.applyJob(
                    jobId,
                    cvId
            );
            return ResponseEntity.ok("Apply job successfully");

    }
    @GetMapping("/api/employer/apply/view/{id}")
    public ResponseEntity<?> viewApplication(
            @PathVariable("id") String id,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "size",defaultValue = "10") Integer size
    ){
            ResponsePageBase<ApplyResponse> response= jobApplicationService.viewApplication(
                    id,
                    page,
                    size
            );
            return ResponseEntity.ok(response);
    }
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
