package com.example.demo.controller;

import com.example.demo.payload.Request.LoginRequest;
import com.example.demo.payload.Request.RegisterEmployerRequest;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Controller
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest
    ){
        Map<String, String> tokens= authService.login(loginRequest.getEmail(),loginRequest.getPassword());
        return ResponseEntity.ok(tokens);
    }
    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(
            @RequestBody Map<String, String> registerRequest)
    {
        try{
            authService.register(
                    registerRequest.get("fullName"),
                    registerRequest.get("email"),
                    registerRequest.get("password"),
                    registerRequest.get("confirmPassword"),
                    registerRequest.get("role")
            );
            return ResponseEntity.ok("Register successfully");
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PostMapping("/api/auth/register-employer")
    public ResponseEntity<?> registerEmployer(
            @RequestBody @Valid RegisterEmployerRequest registerEmployerRequest
            ){
        try{
            authService.registerEmployer(registerEmployerRequest);
            return ResponseEntity.ok("Employer registered successfully");
        }catch (Exception e){
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body("Register failed");
        }
    }
}
