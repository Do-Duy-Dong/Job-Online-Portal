package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.entity.Enum.EnumApprovalStatus;
import com.example.demo.entity.Enum.EnumUserType;
import com.example.demo.exception.ResourceNotFound;
import com.example.demo.payload.Request.CustomStaffPermissionRequest;
import com.example.demo.payload.Response.LoginResponse;
import com.example.demo.payload.Request.RegisterEmployerRequest;
import com.example.demo.payload.Request.CreateEmployerByManagerRequest;

import com.example.demo.payload.Response.RoleWithPermissionsResponse;
import com.example.demo.repository.*;
import com.example.demo.utils.JWTUtil;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;
    private final CompanyService companyService;
    private final RoleRepository roleRepository;
    private final EmployerRepository employerRepository;
    private final EmployeeRepository employeeRepository;
    private final DetailPermissionRepository detailPermissionRepository;
    public AuthService(PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       PermissionRepository permissionRepository,
                       JWTUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       ModelMapper modelMapper,
                       CompanyService companyService,
                       RoleRepository roleRepository,
                       EmployerRepository employerRepository,
                       EmployeeRepository employeeRepository, DetailPermissionRepository detailPermissionRepository) {
        this.permissionRepository = permissionRepository;
        this.modelMapper = modelMapper;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.companyService = companyService;
        this.roleRepository = roleRepository;
        this.employerRepository = employerRepository;
        this.employeeRepository = employeeRepository;
        this.detailPermissionRepository = detailPermissionRepository;
    }

    public User getUserById(String id) {
        return userRepository.findById(java.util.UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public LoginResponse loginEmployee(String email, String password){
        Employee user = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new ResourceNotFound("Invalid password");
        }

        String accessToken = jwtUtil.generateAccessToken(email, null, List.of(user.getRole().getName()), null);
        String refreshToken = jwtUtil.generateRefreshToken(email,null , List.of(user.getRole().getName()), null);
        user.setToken(refreshToken);
        userRepository.save(user);
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    public LoginResponse loginEmployer(String email, String password){
        Employer user = employerRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new ResourceNotFound("Invalid password");
        }
        // Block login if account is pending approval or rejected
        EnumApprovalStatus status = user.getApprovalStatus();
        if (EnumApprovalStatus.PENDING.equals(status)) {
            throw new ResourceNotFound("Your account is pending admin approval");
        }
        if (EnumApprovalStatus.REJECTED.equals(status)) {
            throw new ResourceNotFound("Your account registration has been rejected");
        }

        List<String> permissions= permissionRepository.findPermissionsByUserEmail(email);

        // companyId from Employer entity
        String companyId = user.getCompany().getId().toString();

        String accessToken = jwtUtil.generateAccessToken(email, companyId, List.of(user.getRole().getName()), permissions);
        String refreshToken = jwtUtil.generateRefreshToken(email, companyId, List.of(user.getRole().getName()), permissions);
        user.setToken(refreshToken);
        userRepository.save(user);
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    // Register basic user (EMPLOYEE by default)
    public void register(String fullName, String email, String password, String confirmPassword, String roleName) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            throw new RuntimeException("Email is existed");
        }
        if (!password.equals(confirmPassword)) {
            throw new RuntimeException("Password and confirm password do not match");
        }
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        String hashPass = passwordEncoder.encode(password);
        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPassword(hashPass);
        newUser.setRole(role);
        userRepository.save(newUser);
    }

    // Employer self-register — requires admin approval before login
    @Transactional
    public void registerEmployer(RegisterEmployerRequest request) {
        Optional<User> checkUser = userRepository.findByEmail(request.getEmail());
        if (checkUser.isPresent()) {
            throw new RuntimeException("Email is existed");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password and confirm password do not match");
        }
        Role role = roleRepository.findByName("EMPLOYER")
                .orElseThrow(() -> new RuntimeException("Role EMPLOYER not found"));

        String hashPass = passwordEncoder.encode(request.getPassword());

        Company company = companyService.createCompany(
                request.getCompanyTitle(),
                request.getCompanyAddress(),
                request.getCompanyPhone(),
                request.getCompanyEmail());

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(hashPass);
        user.setRole(role);
        user.setUserType(EnumUserType.EMPLOYER);
        user.setApprovalStatus(EnumApprovalStatus.PENDING);
        User savedUser = userRepository.save(user);

        // Create Employer record linked to this User and Company
        Employer employer = new Employer();
        employer.setUser(savedUser);
        employer.setCompany(company);
        employerRepository.save(employer);
    }

    // MANAGER_HR creates an employer directly — account is immediately APPROVED
    @Transactional
    public void createEmployerByManager(CreateEmployerByManagerRequest request) {
        Optional<User> checkUser = userRepository.findByEmail(request.getEmail());
        if (checkUser.isPresent()) {
            throw new RuntimeException("Email is existed");
        }
        // Use provided roleName, fallback to EMPLOYER
        String roleName = (request.getRoleName() != null && !request.getRoleName().isBlank())
                ? request.getRoleName() : "EMPLOYER";
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        String hashPass = passwordEncoder.encode(request.getPassword());

        Company company = companyService.createCompany(
                request.getCompanyTitle(),
                request.getCompanyAddress(),
                request.getCompanyPhone(),
                request.getCompanyEmail());

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(hashPass);
        user.setRole(role);
        user.setUserType(EnumUserType.EMPLOYER);
        user.setApprovalStatus(EnumApprovalStatus.APPROVED);
        User savedUser = userRepository.save(user);

        Employer employer = new Employer();
        employer.setUser(savedUser);
        employer.setCompany(company);
        employerRepository.save(employer);
    }

    // Admin: approve employer account
    @Transactional
    public void approveEmployer(String userId) {
        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (!EnumUserType.EMPLOYER.equals(user.getUserType())) {
            throw new RuntimeException("User is not an employer");
        }
        if (!EnumApprovalStatus.PENDING.equals(user.getApprovalStatus())) {
            throw new RuntimeException("Employer is not in PENDING state");
        }
        user.setApprovalStatus(EnumApprovalStatus.APPROVED);
        userRepository.save(user);
    }

    // Admin: reject employer account
    @Transactional
    public void rejectEmployer(String userId) {
        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (!EnumUserType.EMPLOYER.equals(user.getUserType())) {
            throw new RuntimeException("User is not an employer");
        }
        if (!EnumApprovalStatus.PENDING.equals(user.getApprovalStatus())) {
            throw new RuntimeException("Employer is not in PENDING state");
        }
        user.setApprovalStatus(EnumApprovalStatus.REJECTED);
        userRepository.save(user);
    }

    // Admin: list all PENDING employers
    public List<User> getPendingEmployers() {
        return userRepository.findByUserTypeAndApprovalStatus(
                EnumUserType.EMPLOYER, EnumApprovalStatus.PENDING);
    }
//    GET list permissions of a role in company
    public List<Role> getPermissioByCoompanyId(String companyId){
        List<Role> roles = roleRepository.findRolesWithCompanyId(UUID.fromString(companyId));
        return roles;
    }
//    Custom permission for company staff
    @Transactional
    public void customPermissionForStaff(CustomStaffPermissionRequest request){
        Role role = roleRepository.findById(UUID.fromString(request.getRoleId()))
                .orElseThrow(() -> new RuntimeException("Role not found"));
        // if (request.getCompanyId() != null && role.getCompany() != null) {
        //     if (!role.getCompany().getId().toString().equals(request.getCompanyId())) {
        //         throw new RuntimeException("Role does not belong to the company");
        //     }
        // }

        List<DetailPermission> detailPermissionList =
                detailPermissionRepository.findDetailPermissionByRoleId(UUID.fromString(request.getRoleId()));

        List<String> requestedPermissionIds =
                request.getListPermissionIdAllow() != null ? request.getListPermissionIdAllow() : List.of();

        Set<UUID> requestedIds = new HashSet<>();
        for (String id : requestedPermissionIds) {
            requestedIds.add(UUID.fromString(id));
        }


        List<UUID> removeDetailPermissionIds = detailPermissionList.stream()
                .filter(entry -> !requestedIds.contains(entry.getId()))
                .map(DetailPermission::getId)
                .toList();

        if (!removeDetailPermissionIds.isEmpty()) {
            detailPermissionRepository.deleteAllById(removeDetailPermissionIds);
        }
        List<UUID> existingPermissionIds = detailPermissionList.stream()
                .map(dp -> dp.getPermission().getId())
                .toList();  
        List<UUID> addPermissionIds = requestedIds.stream()
                .filter(record-> !existingPermissionIds.contains(record))
                .toList();  

        List<Permission> permissionsToAdd = permissionRepository.findAllById(addPermissionIds);
        if (permissionsToAdd.size() != addPermissionIds.size()) {
            throw new RuntimeException("One or more permissions were not found");
        }

        for (Permission permission : permissionsToAdd) {
            DetailPermission newDetailPermission = new DetailPermission();
            newDetailPermission.setPermission(permission);
            newDetailPermission.setRole(role);
            detailPermissionRepository.save(newDetailPermission);
        }

    }
}
