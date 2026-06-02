package com.example.demo.payload.Request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomStaffPermissionRequest {
    private String roleId;
    private String companyId;
    private List<String> listPermissionIdAllow;
}
