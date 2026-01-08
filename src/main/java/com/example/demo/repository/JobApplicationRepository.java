package com.example.demo.repository;

import com.example.demo.entity.Job;
import com.example.demo.entity.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {
    Page<JobApplication> findAllByJob_Id(UUID jobId, Pageable pageable);
    boolean existsByCv_IdAndJob_Id(UUID cvId,UUID jobId);
    boolean existsByCv_Id(UUID cvId);
}
