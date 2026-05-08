package com.example.demo.controller.Employee;

import com.example.demo.entity.CustomUserDetail;
import com.example.demo.payload.Response.JobAllResponse;
import com.example.demo.payload.Request.JobRequest;
import com.example.demo.payload.Response.JobResponse;
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

    /**
     * GET /api/job/getJob/{id}
     * Requires: JOB_READ permission
     */
//    @PreAuthorize("hasAuthority('PERMISSION_JOB_READ')")
    @GetMapping("/api/job/getJob/{id}")
    public ResponseEntity<?> getJobById(@PathVariable String id) {
        try {
            JobResponse job = jobService.getJobById(UUID.fromString(id));
            return ResponseEntity.ok(job);
        } catch (Exception e) {
            log.error("", e);
            return ResponseEntity.status(400).body("Error fetching job");
        }
    }

    /**
     * GET /api/job/getAll
     * Requires: JOB_READ permission
     */
//    @PreAuthorize("hasAuthority('PERMISSION_JOB_READ')")
    @GetMapping("/api/job/getAll")
    public ResponseEntity<?> getAllJob(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "position", required = false) Integer position,
            @RequestParam(value = "salary", required = false) Integer salary,
            @RequestParam(value = "location", required = false) Integer location,
            @RequestParam(value = "cursorTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorTime,
            @RequestParam(value = "cursorId", required = false) String cursorId) {
        try {
            UUID cursorUuid = (cursorId != null && !cursorId.isBlank())
                    ? UUID.fromString(cursorId)
                    : null;
            KeysetPageResponse<JobAllResponse> jobs = jobService.getAllJobs(size, keyword, position, location, salary,
                    cursorTime, cursorUuid);
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body("error fetching");
        }
    }


}
