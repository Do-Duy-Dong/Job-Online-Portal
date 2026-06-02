package com.example.demo.repository;

import com.example.demo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
    @Query("""
            SELECT DISTINCT r FROM Role r
            JOIN FETCH r.detailPermissions dp
            JOIN FETCH dp.permission p
            JOIN r.company c
            WHERE c.id= :companyId
            """)
    List<Role> findRolesWithCompanyId(@Param("companyId") UUID companyId);

}
