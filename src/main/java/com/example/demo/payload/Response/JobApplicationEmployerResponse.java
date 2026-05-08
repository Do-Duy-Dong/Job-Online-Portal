package com.example.demo.payload.Response;

import com.example.demo.utils.statusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationEmployerResponse {
    private UUID applicationId;
    private statusEnum status;
    private String applicantName;
    private String applicantEmail;
    private String cvUrl;
    private UUID cvId;
    private LocalDateTime appliedAt;
}
