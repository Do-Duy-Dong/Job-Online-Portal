package com.example.demo.repository;

import com.example.demo.entity.JobAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JobAssignmentRepository extends JpaRepository<JobAssignment, UUID> {

  /**
   * Kiểm tra employer có được assign vào job hay không.
   */
  @Query("""
      SELECT COUNT(ja) > 0
      FROM JobAssignment ja
      WHERE ja.job.id = :jobId
        AND ja.employer.id = :employerId
      """)
  boolean existsByJobIdAndEmployerId(@Param("jobId") UUID jobId, @Param("employerId") UUID employerId);
}
