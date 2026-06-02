package com.example.demo.repository;

import com.example.demo.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByName(String name);
    @Query("""
            SELECT p.name
            FROM User u
            JOIN  u.role r
            JOIN  r.detailPermissions dp
            JOIN  dp.permission p
            WHERE u.email = :email
            """)
    List<String> findPermissionsByUserEmail(String email);

}
