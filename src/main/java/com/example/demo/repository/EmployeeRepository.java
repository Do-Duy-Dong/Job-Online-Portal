package com.example.demo.repository;

import com.example.demo.entity.Employee;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    @Query("""
            SELECT e 
            FROM Employee e
            JOIN FETCH e.user u
            WHERE u.email = :email
            """)
    Optional<Employee> findByEmail(String email);
}
