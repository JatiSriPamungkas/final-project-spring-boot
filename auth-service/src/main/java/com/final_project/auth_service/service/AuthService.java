package com.final_project.auth_service.service;

import com.final_project.auth_service.dto.request.LoginRequest;
import com.final_project.auth_service.dto.request.RegisterRequest;
import com.final_project.auth_service.dto.response.LoginResponse;
import com.final_project.auth_service.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
