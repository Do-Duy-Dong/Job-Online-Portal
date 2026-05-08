package com.example.demo.config;

import com.example.demo.entity.DetailPermission;
import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import com.example.demo.repository.DetailPermissionRepository;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final DetailPermissionRepository detailPermissionRepository;

    // Permissions for Job CRUD
    private static final String JOB_CREATE_ALL = "JOB_CREATE_ALL";
    private static final String JOB_CREATE_OWN = "JOB_CREATE_OWN";
    private static final String JOB_READ_ALL = "JOB_READ_ALL";
    private static final String JOB_READ_OWN = "JOB_READ_OWN";
    private static final String JOB_UPDATE_ALL = "JOB_UPDATE_ALL";
    private static final String JOB_UPDATE_OWN = "JOB_UPDATE_OWN";
    private static final String JOB_DELETE_ALL = "JOB_DELETE_ALL";
    private static final String JOB_DELETE_OWN = "JOB_DELETE_OWN";
    private static final String CV_STATUS_UPDATE_OWN = "CV_STATUS_UPDATE_OWN";
    private static final String CV_STATUS_UPDATE_ALL = "CV_STATUS_UPDATE_ALL";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Skip if already seeded
        if (permissionRepository.count() > 0) {
            log.info("Permissions already seeded — skipping DataInitializer.");
            return;
        }

        log.info("Seeding permissions, roles, and detail_permissions...");

        // 1. Create Permissions
        Permission createAll = savePermission(JOB_CREATE_ALL, "Tạo job - không giới hạn");
        Permission readAll = savePermission(JOB_READ_ALL, "Xem tất cả cv đã apply bài tuyển dụng");
        Permission readOwn = savePermission(JOB_READ_OWN, "Xem cv bài tuyển dụng mình được assign");
        Permission updateAll = savePermission(JOB_UPDATE_ALL, "Sửa mọi bài tuyển dụng");
        Permission updateOwn = savePermission(JOB_UPDATE_OWN, "Sửa bài tuyển dụng mình được assign");
        Permission deleteAll = savePermission(JOB_DELETE_ALL, "Xoá mọi bài tuyển dụng");
        Permission deleteOwn = savePermission(JOB_DELETE_OWN, "Xoá bài tuyển dụng mình được assign");
        Permission cvStatusOwn = savePermission(CV_STATUS_UPDATE_OWN,
                "Cập nhật trạng thái CV trong job mình được assign");
        Permission cvStatusAll = savePermission(CV_STATUS_UPDATE_ALL, "Cập nhật trạng thái CV của mọi job");
        // 2. Create Roles
        Role adminRole = saveRole("ADMIN");
        Role employerRole = saveRole("EMPLOYER");
        Role employeeRole = saveRole("EMPLOYEE");
        Role hrIntern = saveRole("HR_INTERN");
        Role hr = saveRole("HR");
        Role assistant = saveRole("ASSISTANT");
        Role managerHr = saveRole("MANAGER_HR");

        // 3. Map role → permissions
        assignPermissions(hrIntern, List.of(readOwn, cvStatusOwn));
        assignPermissions(hr, List.of(createAll, readOwn, updateOwn, deleteOwn, cvStatusOwn));
        assignPermissions(assistant, List.of(createAll, readAll));
        assignPermissions(managerHr, List.of(createAll, readAll, updateAll, deleteAll, cvStatusAll));

        log.info("Data seeding completed.");
    }

    private Permission savePermission(String name, String description) {
        Permission p = new Permission();
        p.setName(name);
        p.setDescription(description);
        return permissionRepository.save(p);
    }

    private Role saveRole(String name) {
        Role r = new Role();
        r.setName(name);
        return roleRepository.save(r);
    }

    private void assignPermissions(Role role, List<Permission> permissions) {
        permissions.forEach(permission -> {
            DetailPermission dp = new DetailPermission();
            dp.setRole(role);
            dp.setPermission(permission);
            detailPermissionRepository.save(dp);
        });
    }
}
