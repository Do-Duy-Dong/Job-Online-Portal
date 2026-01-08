package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User extends  BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    private String token;
    private String role;
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

}
