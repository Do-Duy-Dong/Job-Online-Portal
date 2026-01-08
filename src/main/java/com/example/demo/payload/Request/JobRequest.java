package com.example.demo.payload.Request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class JobRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String address;
    @NotBlank
    private String description;
    @NotBlank
    private String requirements;
    @NotNull
    private String benefits;
    @NotNull
    private String schedule;
    private int position;
    private int startRange;
    private int endRange;
    private String status="OPEN";
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expirationDate;
    @NotBlank
    private String categoryId;

    private List<String> tagsId;
}
