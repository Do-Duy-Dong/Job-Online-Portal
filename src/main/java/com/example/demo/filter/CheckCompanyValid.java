package com.example.demo.filter;

import com.example.demo.entity.CustomUserDetail;
import com.example.demo.entity.Job;
import com.example.demo.exception.ResourceNotFound;
import com.example.demo.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("employerCompanyValidator")
@RequiredArgsConstructor
public class CheckCompanyValid {
    private final JobRepository jobRepository;
    public boolean isValidCompany(Authentication authentication,String jobId){
        CustomUserDetail customUserDetail = (CustomUserDetail) authentication.getPrincipal();
        String companyId= customUserDetail.getCompanyId();
        Optional<Job> job=jobRepository.findByIdAndCompanyId(UUID.fromString(jobId),UUID.fromString(companyId));
        if(job.isEmpty()){
            throw new ResourceNotFound("Job not found or you don't have permission to access this job");
        }
        return true;
    }
}
