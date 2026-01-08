package com.example.demo.payload.Response;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class JobAllResponse {
    @NotNull
    private String title;
    @NotNull
    private String address;
    private int startRange;
    private int endRange;
    @NotNull
    private LocalDateTime expirationDate;
    private LocalDateTime createdAt;
    @NotNull
    private String categoryName;
    private String companyName;
}
