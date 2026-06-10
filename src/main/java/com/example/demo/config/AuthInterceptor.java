package com.example.demo.config;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.demo.services.TokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws IOException {
        if (isPublicApi(request)) {
            return true;
        }

        Optional<String> accessToken = getBearerToken(request);
        if (accessToken.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing bearer token");
            return false;
        }

        Optional<Long> studentId = tokenService.validateAccessToken(accessToken.get());
        if (studentId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired bearer token");
            return false;
        }

        if (!canAccessStudentResource(request.getRequestURI(), studentId.get())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token does not match student resource");
            return false;
        }

        request.setAttribute("studentId", studentId.get());
        return true;
    }

    private boolean isPublicApi(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (HttpMethod.OPTIONS.matches(method)) {
            return true;
        }

        if (path.equals("/api/students/register")
            || path.equals("/api/students/login")
            || path.equals("/api/students/refresh-token")) {
            return true;
        }

        return HttpMethod.GET.matches(method) && path.matches("/api/books(/\\d+)?");
    }

    private Optional<String> getBearerToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }

        return Optional.of(authorizationHeader.substring("Bearer ".length()).trim());
    }

    private boolean canAccessStudentResource(String path, Long authenticatedStudentId) {
        if (path.matches("/api/students/\\d+.*")) {
            Long pathStudentId = Long.valueOf(path.split("/")[3]);
            return authenticatedStudentId.equals(pathStudentId);
        }

        if (path.matches("/api/books/\\d+/reserve/\\d+")) {
            Long pathStudentId = Long.valueOf(path.split("/")[5]);
            return authenticatedStudentId.equals(pathStudentId);
        }

        return true;
    }
}
