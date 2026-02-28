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
            LocalDateTime cursorTime,
            UUID cursorId);
}
