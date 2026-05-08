package com.example.demo.payload.Request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ApplyRequest {
    private String jobId;
    private String cvId;
}
