package com.example.demo.entity;

import com.example.demo.entity.Enum.EnumApprovalStatus;
import com.example.demo.entity.Enum.EnumUserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String fullName;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    private String token;
    private EnumUserType userType;
    @Enumerated(EnumType.STRING)
    private EnumApprovalStatus approvalStatus;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

}
