package com.example.demo.repository.impl;

import com.example.demo.repository.JobCustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Job;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JobCustomRepositoryImpl implements JobCustomRepository {
    private static final String[] positionEnum = {
            "Internship",
            "Fresher",
            "Junior",
            "Senior",
            "Lead",
            "Manager"
    };
    private static final String[] rangeSalary = {
            "0-10000000",
            "10000000-20000000",
            "20000000-30000000",
            "30000000-40000000",
            "40000000-50000000",
            "50000000-1000000000"
    };
    private static final String[] locationEnum = {
            "Hanoi",
            "Ho Chi Minh City",
            "Da Nang",
            "Other"
    };
    private final EntityManager em;

    @Override
    public List<Job> findAllJobs(
            int size,
            String keyword,
            Integer position,
            Integer salary,
            Integer location,
            LocalDateTime cursorTime,
            UUID cursorId) {

        Map<String, Object> params = new HashMap<>();
        StringBuilder jpql = new StringBuilder("""
                SELECT j FROM Job j
                JOIN FETCH j.category
                JOIN FETCH j.user u
                JOIN FETCH u.company
                WHERE j.status = 'OPEN'
                """);

        appendFilterClauses(jpql, params, keyword, position,location, salary);

        if (cursorTime != null && cursorId != null) {
            jpql.append("AND (j.createdAt, j.id) <(:cursorTime, :cursorId)\n");
            params.put("cursorTime", cursorTime);
            params.put("cursorId", cursorId);
        }

        jpql.append("ORDER BY j.createdAt DESC, j.id DESC");

        TypedQuery<Job> query = em.createQuery(jpql.toString(), Job.class);
        params.forEach(query::setParameter);
        query.setMaxResults(size);
        return query.getResultList();
    }

    @Override
    public long countAllJobs(String keyword, Integer position, Integer location,Integer salary) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder jpql = new StringBuilder("""
                SELECT COUNT(j) FROM Job j
                WHERE j.status = 'OPEN'
                """);

        appendFilterClauses(jpql, params, keyword, position,location, salary);

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
        params.forEach(query::setParameter);
        return query.getSingleResult();
    }

    private void appendFilterClauses(
            StringBuilder jpql,
            Map<String, Object> params,
            String keyword,
            Integer position,
            Integer location,
            Integer salary) {

        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim().toLowerCase() : null;
        if (kw != null) {
            jpql.append("AND LOWER(j.title) ILIKE CONCAT('%', :keyword, '%')\n");
            params.put("keyword", kw);
        }
        if (location != null && location >= 0 && location < 4) {
            jpql.append("AND j.location = :location\n");
            params.put("location", locationEnum[location]);
        }
        if (position != null && position >= 0 && position < positionEnum.length) {
            jpql.append("AND j.position = :position\n");
            params.put("position", positionEnum[position]);
        }

        if (salary != null && salary >= 0 && salary < rangeSalary.length) {
            String[] parts = rangeSalary[salary].split("-");
            int salaryMin = Integer.parseInt(parts[0]);
            int salaryMax = Integer.parseInt(parts[1]);
            jpql.append("AND j.startRange >= :salaryMin\n");
            jpql.append("AND j.endRange <= :salaryMax\n");
            params.put("salaryMin", salaryMin);
            params.put("salaryMax", salaryMax);
        }
    }
}
