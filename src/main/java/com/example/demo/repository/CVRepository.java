package com.example.demo.repository;


import com.example.demo.entity.CV;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CVRepository extends JpaRepository<CV,UUID> {
    Optional<CV> findByEmployee_IdAndIsActiveTrue(UUID employeeId);
    List<CV> findAllByEmployee_IdAndIsActiveTrue(UUID employeeId);
}
