package com.example.demo.payload.Response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApplyResponse {
    private String name;
    private String email;
    private String cvUrl;
    private LocalDateTime appliedAt;
    public ApplyResponse() {
    }
    public ApplyResponse(String name, String email, String cvUrl, LocalDateTime appliedAt) {
        this.name = name;
        this.email = email;
        this.cvUrl = cvUrl;
        this.appliedAt = appliedAt;
    }
}
