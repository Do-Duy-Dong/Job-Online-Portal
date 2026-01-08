package com.example.demo.service;

import com.example.demo.entity.Company;
import com.example.demo.entity.User;
import com.example.demo.payload.Request.RegisterEmployerRequest;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.JWTUtil;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;
    private final CompanyService companyService;
//    @Autowired
    public AuthService(PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       JWTUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       ModelMapper modelMapper,
                       CompanyService companyService){
        this.modelMapper= modelMapper;
        this.authenticationManager= authenticationManager;
        this.passwordEncoder= passwordEncoder;
        this.userRepository= userRepository;
        this.jwtUtil= jwtUtil;
        this.companyService= companyService;
    }
    public User getUserById(String id){
        return userRepository.findById(java.util.UUID.fromString(id))
                .orElseThrow(()-> new EntityNotFoundException("User not found"));
    }

    public Map<String,String> login(String email, String password){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );
        User user= userRepository.findByEmail(email)
                .orElseThrow(()-> new EntityNotFoundException("User not found"));

        String accessToken= jwtUtil.generateAccessToken(user.getEmail().toString());
        String refreshToken= jwtUtil.generateRefreshToken(user.getEmail().toString());
        user.setToken(refreshToken);
        userRepository.save(user);
        return Map.of(
                "accessToken",accessToken,
                "refreshToken",refreshToken
        );
    }
//   Register user
    public void register(String fullName, String email, String password, String confirmPassword, String role){
        Optional<User> user= userRepository.findByEmail(email);
        if(user.isPresent()){
            throw new RuntimeException("Email is existed");
        }
        if(!password.equals(confirmPassword)){
            throw new RuntimeException("Password and confirm password do not match");
        }
        String hashPass= passwordEncoder.encode(password);
        User newUser= new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPassword(hashPass);
        newUser.setRole(role);
        userRepository.save(newUser);
    }
//    Employer register
    public void registerEmployer (
        RegisterEmployerRequest request
    ){
        Optional<User> checkUser= userRepository.findByEmail(request.getEmail());
        if(checkUser.isPresent()){
            throw new RuntimeException("Email is existed");
        }
        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new RuntimeException("Password and confirm password do not match");
        }
        String hashPass= passwordEncoder.encode(request.getPassword());

        Company company = companyService.createCompany(
                request.getCompanyTitle(),
                request.getCompanyAddress(),
                request.getCompanyPhone(),
                request.getCompanyEmail()
        );
        User user= new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(hashPass);
        user.setRole("EMPLOYER");
        user.setCompany(company);
        userRepository.save(user);
    }
}
