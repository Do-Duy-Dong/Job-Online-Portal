package com.example.demo.repository;


import com.example.demo.entity.CV;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CVRepository extends JpaRepository<CV,UUID> {
    @Query("""
            SELECT c FROM CV c
            JOIN FETCH c.employee e
            JOIN FETCH e.user u
            WHERE c.id = :id
                AND c.isActive = true
                AND u.email= :email
            """)
    Optional<CV> findByCvIdAndEmail(UUID id, String email);
    Optional<CV> findByCvNameAndEmail(String cvName, String email);
    Optional<CV> findByEmployee_IdAndIsActiveTrue(UUID employeeId);
    List<CV> findAllByEmployee_IdAndIsActiveTrue(UUID employeeId);
}
