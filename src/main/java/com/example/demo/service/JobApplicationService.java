package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFound;
import com.example.demo.payload.Response.ApplyResponse;
import com.example.demo.payload.Response.ListJobApplication;
import com.example.demo.payload.Response.ResponsePageBase;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.JobApplicationRepository;
import com.example.demo.utils.statusEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JobApplicationService {
    @Value("${job.urlWeb}")
    private String joblink;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobService jobService;
    private final CvService cvService;
    private final MailService mailService;
    private final EmployeeRepository employeeRepository;

    public JobApplicationService(
            JobApplicationRepository jobApplicationRepository,
            JobService jobService,
            CvService cvService,
            MailService mailService,
            EmployeeRepository employeeRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobService = jobService;
        this.cvService = cvService;
        this.mailService = mailService;
        this.employeeRepository = employeeRepository;
    }

    public JobApplication findById(UUID id) {
        return jobApplicationRepository.findById(id).orElseThrow(
                () -> new ResourceNotFound("Job application not found"));
    }

    public boolean checkUserFromApplyJob(UUID cvId, UUID jobId) {
        return jobApplicationRepository.existsByEmployee_IdAndJob_Id(cvId, jobId);
    }

    public boolean checkCvFromApplyJob(UUID cvId) {
        return jobApplicationRepository.existsByCv_Id(cvId);
    }

    // POST [api/job-apply]
    public void applyJob(
            String jobId,
            String cvId,
            String email) {
        // Check job and cv exist and true user
        Job job = jobService.findById(UUID.fromString(jobId));
        CV cv = cvService.findById(UUID.fromString(cvId), email);
        Employee employee = cv.getEmployee();

        if (checkUserFromApplyJob(cv.getId(), job.getId())) {
            throw new IllegalArgumentException("You have already applied this job");
        }

        JobApplication jobApply = new JobApplication();
        jobApply.setJob(job);
        jobApply.setEmployee(employee);
        jobApply.setCv(cv);
        jobApplicationRepository.save(jobApply);

        // Mail for applicant
        Map<String, Object> map = new HashMap<>();
        map.put("userName", employee.getUser().getFullName());
        map.put("jobTitle", job.getTitle());
        map.put("jobLink", joblink + "/job-detail/" + job.getId());
        mailService.sendMail(
                employee.getUser().getEmail(),
                "Application Received for " + job.getTitle(),
                map);
    }

    // GET [api/employee/applications] - Xem danh sách job đã apply
    public ResponsePageBase<ListJobApplication> getMyApplications(
            UUID employeeId,
            Integer page,
            Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ListJobApplication> applications = jobApplicationRepository.findAllApplicationByEmployeeId(employeeId,
                pageable);
        return ResponsePageBase.<ListJobApplication>builder()
                .content(applications.getContent())
                .currentPage(applications.getNumber() + 1)
                .totalPage(applications.getTotalPages())
                .build();
    }

    // PATCH [api/employee/applications/{id}/cancel] - Huỷ application
    // CHưa tôi ưu n+1 query cho employee
    public void cancelApplication(String applicationId, UUID employeeId) {
        JobApplication jobApplication = findById(UUID.fromString(applicationId));
        if (!jobApplication.getEmployee().getUser().getId().equals(employeeId)) {
            throw new IllegalArgumentException("You are not authorized to cancel this application");
        }
        if (jobApplication.getStatus() == statusEnum.CANCELLED) {
            throw new IllegalArgumentException("Application is already cancelled");
        }
        if (jobApplication.getStatus() == statusEnum.ACCEPTED) {
            throw new IllegalArgumentException("You cannot cancel an accepted application");
        }
        jobApplication.setStatus(statusEnum.CANCELLED);
        jobApplicationRepository.save(jobApplication);
    }

    // EMPLOYER
    // Chưa tối ưu n+1 query cho employee
    // [api/employer/view/{id}]
    public ResponsePageBase<ApplyResponse> viewApplication(
            String jobId,
            Integer page,
            Integer size,
            UUID companyId) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<JobApplication> applies = jobApplicationRepository.findAllByJobIdWithCompany(
                UUID.fromString(jobId),
                companyId,
                pageable);
        List<ApplyResponse> response = applies.stream().map(
                item -> {
                    ApplyResponse dto = new ApplyResponse(
                            item.getEmployee().getUser().getFullName(),
                            item.getEmployee().getUser().getEmail(),
                            cvService.getSignUrl(item.getCv().getUrl()),
                            item.getCreatedAt());
                    return dto;
                }).toList();
        ResponsePageBase<ApplyResponse> responsePageBase = ResponsePageBase.<ApplyResponse>builder()
                .content(response)
                .currentPage(applies.getNumber() + 1)
                .totalPage(applies.getTotalPages())
                .build();
        return responsePageBase;
    }

    // [api/employer/update-status]
    public void updateStatusApplies(
            String applicationId,
            statusEnum status) {
        JobApplication jobApplication = findById(UUID.fromString(applicationId));
        jobApplication.setStatus(status);
        jobApplicationRepository.save(jobApplication);
    }
}
