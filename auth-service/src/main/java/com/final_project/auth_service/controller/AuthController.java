package com.final_project.auth_service.controller;

import com.final_project.auth_service.dto.request.LoginRequest;
import com.final_project.auth_service.dto.request.RegisterRequest;
import com.final_project.auth_service.dto.response.BaseResponse;
import com.final_project.auth_service.dto.response.LoginResponse;
import com.final_project.auth_service.dto.response.UserResponse;
import com.final_project.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse<>(HttpStatus.CREATED.value(), "User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(new BaseResponse<>(HttpStatus.OK.value(), "Login successful", response));
    }
}
