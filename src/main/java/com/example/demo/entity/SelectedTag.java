package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "selected_tag")
public class SelectedTag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;
    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public SelectedTag() {
    }
    public SelectedTag(Job job, Tag tag) {
        this.job = job;
        this.tag = tag;
    }
}
