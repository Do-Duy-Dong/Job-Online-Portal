package com.example.demo.repository;

import com.example.demo.entity.DetailPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DetailPermissionRepository extends JpaRepository<DetailPermission, UUID> {
}
