package com.example.demo.repository;

import com.example.demo.entity.Job;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {
    Page<Job> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Job> findAllByStatus(String status, Specification<Job> spec,Pageable pageable);
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE job 
            SET status = 'CLOSED' 
            WHERE id IN (
                SELECT id
                FROM job 
                WHERE status = 'OPEN' AND expiration_date < NOW()
                LIMIT :limit
            ) 
            """, nativeQuery = true)
    int updateExpiredJobs(@Param("limit") int limit);
}
