package com.example.demo.services;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.AuthResponse;
import com.example.demo.dtos.LoginRequest;
import com.example.demo.dtos.StudentAuthData;
import com.example.demo.repositories.StudentRepository;

@Service
public class AuthService {

    private final StudentRepository studentRepository;
    private final TokenService tokenService;

    public AuthService(StudentRepository studentRepository, TokenService tokenService) {
        this.studentRepository = studentRepository;
        this.tokenService = tokenService;
    }

    public Optional<AuthResponse> login(LoginRequest loginRequest) {
        return studentRepository.findAuthDataByEmail(loginRequest.getEmail())
            .filter(authData -> passwordMatches(loginRequest.getPassword(), authData))
            .map(authData -> tokenService.issueTokens(authData.getStudentId()));
    }

    public Optional<AuthResponse> refreshToken(String refreshToken) {
        return tokenService.refreshAccessToken(refreshToken);
    }

    private boolean passwordMatches(String rawPassword, StudentAuthData authData) {
        if (rawPassword == null || authData.getPasswordHash() == null || authData.getPasswordHash().isBlank()) {
            return false;
        }

        return BCrypt.checkpw(rawPassword, authData.getPasswordHash());
    }
}
