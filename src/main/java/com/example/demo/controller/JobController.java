package com.example.demo.controller;

import com.example.demo.payload.Response.JobAllResponse;
import com.example.demo.payload.Request.JobRequest;
import com.example.demo.payload.Response.JobResponse;
import com.example.demo.payload.Response.ResponsePageBase;
import com.example.demo.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Controller
//@RequestMapping("/api/job")
public class JobController {

    private final JobService jobService;
    public JobController(JobService jobService){
        this.jobService= jobService;
    }

    @GetMapping("/api/job/getJob/{id}")
    public ResponseEntity<?> getJobById(
            @PathVariable String id
    ){
        try{
            JobResponse job= jobService.getJobById(UUID.fromString(id));
            return ResponseEntity.ok(job);
        } catch (Exception e) {
            log.error("",e);
            return ResponseEntity.status(400).body("Error fetching job");
        }
    }
    @GetMapping("/api/job/getAll")
    public ResponseEntity<?> getAllJob(
            @RequestParam(value = "keyword",required = false) String keyword,
            @RequestParam(value = "page",defaultValue = "1") int page,
            @RequestParam(value = "sortKey", defaultValue = "createdAt") String sortKey,
            @RequestParam(value = "sortValue", defaultValue = "desc") String sortValue,
            @RequestParam(value = "size",defaultValue = "10") int size,
            @RequestParam(value="position",required = false) Integer position,
            @RequestParam(value = "salary", required = false) Integer salary
    ){
        try{
            ResponsePageBase<JobAllResponse> jobs= jobService.getAllJobs(page,size,keyword,sortKey,sortValue,position,salary);
            return ResponseEntity.ok(jobs);
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(400).body("error fetching");
        }

    }
    @PostMapping("api/employer/job/create")
    public ResponseEntity<?> createJob(
            @RequestBody JobRequest jobRequest,
            Authentication authentication
            ){
        try{
            String email= authentication.getName();
            jobService.createJob(jobRequest,email);
            return ResponseEntity.ok("Job created successfully");
        }
        catch(Exception e){
            log.error("Error creating job: {}", e);
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

}
