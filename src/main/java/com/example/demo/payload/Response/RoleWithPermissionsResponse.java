package com.example.demo.payload.Response;

import com.example.demo.entity.Permission;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleWithPermissionsResponse {
    private String roleName;
    private List<Permission> permissions;

}
