package com.example.demo.entity;

import com.example.demo.utils.statusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class JobApplication extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private statusEnum status ;
    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;
    @ManyToOne
    @JoinColumn(name ="employee_id")
    private User employee;
    @ManyToOne
    @JoinColumn(name = "cv_id")
    private CV cv;


}
