package com.example.demo.payload.Response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListCvResponse {
    private String fileName;
    private UUID id;
}
