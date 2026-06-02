package com.example.demo.repository;

import com.example.demo.entity.DetailPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DetailPermissionRepository extends JpaRepository<DetailPermission, UUID> {
    @Query("""
                SELECT dp FROM Role r
              JOIN FETCH r.detailPermissions dp
              JOIN dp.permission p
              WHERE r.id= :id
            """)
    List<DetailPermission> findDetailPermissionByRoleId(@Param("id") UUID roleId);
}
