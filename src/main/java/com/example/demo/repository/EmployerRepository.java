package com.example.demo.repository;

import com.example.demo.entity.Employer;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface EmployerRepository extends JpaRepository<Employer, UUID> {
    @Query("""
            SELECT e 
            FROM Employer e
            JOIN FETCH e.user u
            WHERE u.email = :email
            """)
    Optional<Employer> findByEmail(String email);
    @Query("""
            SELECT e
            FROM Employer e
            JOIN FETCH e.user u
            JOIN FETCH e.company c
            WHERE u.email= :email
            """)
    Optional<Employer> findCompanyByEmployerEmail(String email);
}
