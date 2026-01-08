package com.example.demo.payload.Response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserResponseDTO {
    private UUID id;
    private String email;
    private String fullName;
    private String role;

}
