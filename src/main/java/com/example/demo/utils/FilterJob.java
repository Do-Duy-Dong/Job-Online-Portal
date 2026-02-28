package com.example.demo.utils;

import com.example.demo.entity.Job;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class FilterJob {
    private static final String[] rangeSalary = {
            "0-10000000",
            "10000000-20000000",
            "20000000-30000000",
            "30000000-40000000",
            "40000000-50000000",
            "50000000-1000000000"
    };
    private static final String[] positionEnum = {
            "Internship",
            "Fresher",
            "Junior",
            "Senior",
            "Lead",
            "Manager"
    };

    /** Filter gốc — dùng cho offset pagination (giữ nguyên để tương thích) */
    public static Specification<Job> filter(Integer salary, Integer position, String keyword) {
        return buildFilterSpec(salary, position, keyword);
    }

    /**
     * Filter cho keyset pagination.
     * Giống filter() nhưng thêm cursor predicate nếu cursor không null.
     *
     * @param cursorTime createdAt của item cuối trang trước (null = trang đầu)
     * @param cursorId   id của item cuối trang trước (null = trang đầu)
     */
    public static Specification<Job> filterKeyset(
            Integer salary,
            Integer position,
            String keyword,
            LocalDateTime cursorTime,
            UUID cursorId) {
        Specification<Job> spec = buildFilterSpec(salary, position, keyword);

        // Chỉ thêm cursor nếu cả hai không null (trang đầu tiên không có cursor)
        if (cursorTime != null && cursorId != null) {
            spec = spec.and(JobSpecification.hasCursor(cursorTime, cursorId));
            System.out.println("apply keyset");
        }

        return spec;
    }

    // ---------------------------------------------------------------
    // Private helper — xây phần filter chung (salary, position, keyword, expired)
    // ---------------------------------------------------------------
    private static Specification<Job> buildFilterSpec(Integer salary, Integer position, String keyword) {
        Specification<Job> spec = Specification.where(null);

        if (salary != null && salary >= 0 && salary < rangeSalary.length) {
            String[] parts = rangeSalary[salary].split("-");
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            spec = spec.and(JobSpecification.hasSalary(from, to));
        }
        if (position != null && position >= 0 && position < positionEnum.length) {
            spec = spec.and(JobSpecification.hasPosition(positionEnum[position]));
        }
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and(JobSpecification.hasTitle(keyword));
        }
        spec = spec.and(JobSpecification.isNotExpired());

        return spec;
    }
}
