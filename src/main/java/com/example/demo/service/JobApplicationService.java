package com.example.demo.service;

import com.example.demo.entity.CV;
import com.example.demo.entity.Job;
import com.example.demo.entity.JobApplication;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFound;
import com.example.demo.payload.Response.ApplyResponse;
import com.example.demo.payload.Response.ResponsePageBase;
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
    public JobApplicationService(
            JobApplicationRepository jobApplicationRepository,
            JobService jobService,
            CvService cvService,
            MailService mailService
    ){
        this.jobApplicationRepository= jobApplicationRepository;
        this.jobService= jobService;
        this.cvService= cvService;
        this.mailService= mailService;
    }
    public JobApplication findById(UUID id){
        return jobApplicationRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFound("Job application not found")
        );
    }
    public boolean checkUserFromApplyJob(UUID cvId,UUID jobId){
        return jobApplicationRepository.existsByCv_IdAndJob_Id(cvId,jobId);
    }
    public boolean checkCvFromApplyJob(UUID cvId){
        return jobApplicationRepository.existsByCv_Id(cvId);
    }

//    POST [api/job-apply]
    public void applyJob(
            String jobId,
            String cvId
    ){
        Job job= jobService.getJobEntityById(UUID.fromString(jobId));
        CV cv = cvService.findById(UUID.fromString(cvId));
        if(checkUserFromApplyJob(cv.getId(),job.getId())){
            throw new IllegalArgumentException("You have already applied with this CV");
        }
        User user= cv.getEmployee();
        JobApplication jobApply= new JobApplication();
        jobApply.setJob(job);
        jobApply.setEmployee(user);
        jobApply.setCv(cv);
        jobApplicationRepository.save(jobApply);

//        Mail for applicant
        Map<String, Object> map= new HashMap<>();
        map.put("userName",user.getFullName());
        map.put("jobTitle",job.getTitle());
        map.put("jobLink",joblink+"/job-detail/"+job.getId());
        mailService.sendMail(
                user.getEmail(),
                "Application Received for " + job.getTitle(),
                map
        );
    }
//    [api/employer/view/{id}]
    public ResponsePageBase<ApplyResponse> viewApplication(
            String jobId,
            Integer page,
            Integer size
    ){
        Pageable pageable= PageRequest.of(page-1,size);
        Page<JobApplication> applies= jobApplicationRepository.findAllByJob_Id(
                UUID.fromString(jobId),
                pageable
        );
        List<ApplyResponse> response= applies.stream().map(
                item->{
                    ApplyResponse dto= new ApplyResponse(
                            item.getEmployee().getFullName(),
                            item.getEmployee().getEmail(),
                            item.getCv().getUrl(),
                            item.getCreatedAt()
                    );
                    return dto;
                }
        ).toList();
        ResponsePageBase<ApplyResponse> responsePageBase  =  ResponsePageBase.<ApplyResponse>builder()
                .content(response)
                .currentPage(applies.getNumber()+1)
                .totalPage(applies.getTotalPages())
                .build();
        return responsePageBase;
    }
//    [api/employer/update-status]
    public void updateStatusApplies(
            String applicationId,
            statusEnum status
    ){
        JobApplication jobApplication= findById(UUID.fromString(applicationId));
        jobApplication.setStatus(status);
        jobApplicationRepository.save(jobApplication);
    }
}
