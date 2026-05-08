package com.example.demo.repository;

import com.example.demo.entity.Enum.EnumApprovalStatus;
import com.example.demo.entity.Enum.EnumUserType;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    @Query("""
            SELECT u FROM User u
            JOIN FETCH u.role r
            JOIN FETCH r.detailPermissions dp
            JOIN FETCH dp.permission p
            JOIN FETCH u.employer e
            JOIN FETCH e.company c
            WHERE u.email = :email
            """)
    Optional<User> getFullEmployeeFetch(String email);

    List<User> findByUserTypeAndApprovalStatus(EnumUserType userType, EnumApprovalStatus approvalStatus);
}
