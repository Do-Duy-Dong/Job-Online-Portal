package com.example.demo.repository;

import com.example.demo.entity.Job;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job>, JobCustomRepository {
    Page<Job> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @EntityGraph(attributePaths = { "category", "employer.company" })
    Optional<Job> findDetailById(UUID id);

    @EntityGraph(attributePaths = { "category", "employer.company" })
    Page<Job> findAll(Specification<Job> spec, Pageable pageable);
//    Query check company before get job
    @Query("""
            SELECT j FROM Job j
            JOIN FETCH j.employer e
            JOIN FETCH e.company c
            WHERE j.id = :id AND c.id = :companyId)
            """)
    Optional<Job> findByIdAndCompanyId(UUID id, UUID companyId);

    @Query(value = """
            SELECT * FROM public.job j
            where
            status ='OPEN'
            AND (j.created_at, j.id) < ('2025-08-21T17:12:52.181668', '1e4c93c7-2a09-45fc-aca5-6843c21aae59')
            and j.start_range >10000000
            and j.end_range <20000000
            order by j.created_at desc, id desc
            limit 20
                """, nativeQuery = true)
    List<Job> findJob();

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

    @Query("""
            SELECT j FROM Job j
            JOIN FETCH j.employer e
            JOIN FETCH e.company c
            JOIN FETCH j.category
            WHERE c.id = :companyId
            AND j.status = 'OPEN'
            AND (:cursorTime IS NULL OR j.createdAt < :cursorTime
                OR (j.createdAt = :cursorTime AND j.id < :cursorId))
            ORDER BY j.createdAt DESC, j.id DESC
            """)
    List<Job> findAllByCompanyId(
            @Param("companyId") UUID companyId,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") UUID cursorId,
            Pageable pageable);
}
