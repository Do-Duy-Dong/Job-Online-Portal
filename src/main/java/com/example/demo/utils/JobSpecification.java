package com.example.demo.utils;

import com.example.demo.entity.Job;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

public class JobSpecification {

    public static Specification<Job> hasPosition(String position) {
        return (root, query, cb) -> cb.equal(root.get("position"), position);
    }

    public static Specification<Job> hasSalary(int startRange, int endRange) {
        return (root, query, cb) -> cb.not(
                cb.or(
                        cb.greaterThan(root.get("startRange"), endRange),
                        cb.lessThan(root.get("endRange"), startRange)));
    }

    public static Specification<Job> hasTitle(String title) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Job> isNotExpired() {
        return (root, query, cb) -> cb.equal(root.get("status"), "OPEN");
    }

    /**
     * Keyset cursor predicate — thay thế hoàn toàn cho OFFSET.
     *
     * Điều kiện tương đương: (createdAt, id) < (cursorTime, cursorId) theo DESC:
     * (createdAt < cursorTime)
     * OR (createdAt = cursorTime AND id < cursorId)
     *
     * JPA Criteria API sinh ra SQL chuẩn, optimizer vẫn có thể dùng
     * composite index (created_at, id) cho điều kiện này.
     */
    public static Specification<Job> hasCursor(LocalDateTime cursorTime, UUID cursorId) {
        return (root, query, cb) -> {
            // Nhánh 1: created_at < cursorTime
            var beforeTime = cb.lessThan(root.<LocalDateTime>get("createdAt"), cursorTime);

            // Nhánh 2: created_at = cursorTime AND id < cursorId (so sánh UUID as String)
            var sameTime = cb.equal(root.get("createdAt"), cursorTime);
            var beforeId = cb.lessThan(root.get("id"), cursorId);
            var tiebreak = cb.and(sameTime, beforeId);

            return cb.or(beforeTime, tiebreak);
        };
    }
}
