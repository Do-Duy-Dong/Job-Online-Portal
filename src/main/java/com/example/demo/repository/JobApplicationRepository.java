package com.example.demo.repository;

import com.example.demo.entity.Job;
import com.example.demo.entity.JobApplication;
import com.example.demo.payload.Response.ListJobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {
    @Query(value = """
            SELECT *
            FROM job_application ja
            JOIN users u ON ja.employee_id = u.id
            WHERE ja.job_id = :jobId
            AND u.company_id = :companyId
            """, nativeQuery = true)
    Page<JobApplication> findAllByJobIdWithCompany(UUID jobId, UUID companyId, Pageable pageable);

    @Query("""
            SELECT new com.example.demo.payload.Response.ListJobApplication(
                ja.id,
                ja.status,
                j.id,
                v.id,
                j.title,
                c.title
            )
            FROM JobApplication ja
            JOIN  ja.job j
            JOIN  ja.cv v
            JOIN  j.user u
            JOIN  u.company c
            WHERE ja.employee.id = :employeeId
            """)
    Page<ListJobApplication> findAllApplicationByEmployeeId(UUID employeeId, Pageable pageable);

    boolean existsByEmployee_IdAndJob_Id(UUID employeeId, UUID jobId);

    boolean existsByCv_Id(UUID cvId);
}
