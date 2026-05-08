package com.example.demo.config;

import com.example.demo.entity.CustomUserDetail;
import com.example.demo.entity.Employer;
import com.example.demo.entity.Job;
import com.example.demo.exception.ResourceNotFound;
import com.example.demo.repository.EmployerRepository;
import com.example.demo.repository.JobAssignmentRepository;
import com.example.demo.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component("employerJobValidator")
@RequiredArgsConstructor
public class EmployerJobValidator {

    private final JobRepository jobRepository;
    private final EmployerRepository employerRepository;
    private final JobAssignmentRepository jobAssignmentRepository;


    public boolean canAccessJob(Authentication authentication, String jobId, String permissionBase) {

        // Tầng 1: job phải thuộc company của employer
        UUID jobUuid = UUID.fromString(jobId);
        CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
        validateCompanyOwnership(userDetail, jobUuid);

        // Tầng 2: employer phải có quyền
        String allPermission = "PERMISSION_" + permissionBase + "_ALL";
        String ownPermission = "PERMISSION_" + permissionBase + "_OWN";
        boolean hasAll = hasAuthority(authentication, allPermission);
        boolean hasOwn = hasAuthority(authentication, ownPermission);

        if (!hasAll && !hasOwn) {
            throw new AccessDeniedException(
                    "Access denied: requires " + allPermission + " or " + ownPermission);
        }

        // Có ALL permission → bypass tầng 1 & 2
        if (hasAll) {
            log.debug("Access granted via ALL permission [{}] for job [{}]", allPermission, jobId);
            return true;
        }



        // Tầng 3: employer phải được assign vào job
        validateJobAssignment(userDetail, jobUuid);

        log.debug("Access granted via OWN permission [{}] for job [{}]", ownPermission, jobId);
        return true;
    }

    private void validateCompanyOwnership(CustomUserDetail userDetail, UUID jobUuid) {
        String companyId = userDetail.getCompanyId();
        if (companyId == null || companyId.isBlank()) {
            throw new AccessDeniedException("Access denied: employer has no associated company");
        }

        Job job = jobRepository.findByIdAndCompanyId(jobUuid, UUID.fromString(companyId))
                .orElseThrow(() -> new AccessDeniedException(
                        "Access denied: job does not belong to your company"));

        log.debug("Layer 1 passed – job [{}] belongs to company [{}]", jobUuid, companyId);
    }

    private void validateJobAssignment(CustomUserDetail userDetail, UUID jobUuid) {
        String email = userDetail.getUsername();

        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFound("Employer not found: " + email));

        boolean assigned = jobAssignmentRepository.existsByJobIdAndEmployerId(jobUuid, employer.getId());
        if (!assigned) {
            throw new AccessDeniedException(
                    "Access denied: you are not assigned to this job");
        }

        log.debug("Layer 2 passed – employer [{}] is assigned to job [{}]", email, jobUuid);
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }


}
