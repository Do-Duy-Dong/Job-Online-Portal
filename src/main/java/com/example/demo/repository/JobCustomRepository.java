package com.example.demo.repository;

import com.example.demo.entity.Job;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface JobCustomRepository {
    List<Job> findAllJobs(
            int size,
            String keyword,
            Integer position,
            Integer salary,
            Integer location,
            LocalDateTime cursorTime,
            UUID cursorId);

    long countAllJobs(String keyword, Integer position, Integer location, Integer salary);
}
