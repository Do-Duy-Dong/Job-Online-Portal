package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class CV {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String url;
    private boolean isActive=true;
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private User employee;
}
