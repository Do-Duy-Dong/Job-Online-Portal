package com.example.demo.repository.impl;

import com.example.demo.repository.JobCustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static String[] positionEnum = {
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

    private final EntityManager em;

    public List<Job> findAllJobs(
            int size,
            String keyword,
            Integer position,
            Integer salary,
            LocalDateTime cursorTime,
           UUID cursorId) {
        String positionStr = (position != null && position >= 0 && position < positionEnum.length)
                ? positionEnum[position]
                : null;

        Integer salaryMin = null, salaryMax = null;
        if (salary != null && salary >= 0 && salary < rangeSalary.length) {
            String[] parts = rangeSalary[salary].split("-");
            salaryMin = Integer.parseInt(parts[0]);
            salaryMax = Integer.parseInt(parts[1]);
        }

        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        StringBuilder jpql = new StringBuilder("""
                SELECT j FROM Job j
                JOIN FETCH j.category
                JOIN FETCH j.user u
                JOIN FETCH u.company
                WHERE j.status = 'OPEN'
                """);
        Map<String, Object> params = new HashMap<>();
        if (kw != null) {
            jpql.append("AND LOWER(j.title) LIKE CONCAT ('%', :keyword, '%')\n");
            params.put("keyword", kw.toLowerCase());
        }
        if (positionStr != null) {
            jpql.append("AND j.position = :position\n");
            params.put("position", positionStr);
        }
        if (salaryMin != null) {
            jpql.append("AND j.startRange >= :salaryMin\n");
            params.put("salaryMin", salaryMin);
        }
        if (salaryMax != null) {
            jpql.append("AND j.endRange <= :salaryMax\n");
            params.put("salaryMax", salaryMax);
        }

        if (cursorTime != null && cursorId != null) {
            jpql.append("""
                    AND (j.createdAt, j.id) <(:cursorTime, :cursorId)
                    """);
            params.put("cursorTime", cursorTime);
            params.put("cursorId", cursorId);
        }

        jpql.append("ORDER BY j.createdAt DESC, j.id DESC");

        TypedQuery<Job> query = em.createQuery(jpql.toString(), Job.class);

        params.forEach(query::setParameter);

        query.setMaxResults(size);
        return query.getResultList();
    }
}
