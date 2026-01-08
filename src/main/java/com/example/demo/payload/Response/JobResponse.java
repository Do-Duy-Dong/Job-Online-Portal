package com.example.demo.payload.Response;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class JobResponse {
    @NotNull
    private String title;
    @NotNull
    private String address;
    @NotNull
    private String description;
    @NotNull
    private String requirements;
    @NotNull
    private String benefits;
    @NotNull
    private String schedule;
    private String position;
    private int startRange;
    private int endRange;
    @NotNull
    private String status;
    @NotNull
    private LocalDateTime expirationDate;
    private Date createdAt;
    @NotNull
    private String categoryName;
    private String companyName;
    private String companyId;
    private List<Map<String,String>> tags;

}
