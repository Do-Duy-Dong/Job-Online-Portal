package com.example.demo.repository;

import com.example.demo.entity.Job;
import com.example.demo.entity.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {
    @Query(value= """
            SELECT *
            FROM job_application ja
            JOIN users u ON ja.employee_id = u.id
            WHERE ja.job_id = :jobId
            AND u.company_id = :companyId
            """,
    nativeQuery = true)
    Page<JobApplication> findAllByJobIdWithCompany(UUID jobId, UUID companyId, Pageable pageable);
    boolean existsByCv_IdAndJob_Id(UUID cvId,UUID jobId);
    boolean existsByCv_Id(UUID cvId);
}
