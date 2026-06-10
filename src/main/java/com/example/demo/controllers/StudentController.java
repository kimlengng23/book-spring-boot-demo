package com.example.demo.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dtos.AuthResponse;
import com.example.demo.dtos.LoginRequest;
import com.example.demo.dtos.RefreshTokenRequest;
import com.example.demo.dtos.RegisterStudentRequest;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.models.Student;
import com.example.demo.services.AuthService;
import com.example.demo.services.BookService;
import com.example.demo.services.StudentService;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;
    private final AuthService authService;

    public StudentController(StudentService studentService, BookService bookService, AuthService authService) {
        this.studentService = studentService;
        this.authService = authService;
    }

    @GetMapping("/get")
    public ResponseEntity<Student> getStudent(@RequestAttribute("studentId") Long studentId) {
        Optional<Student> student = studentService.getStudentById(studentId);
        if (student.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student.get());
    }
    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(@RequestBody RegisterStudentRequest registerStudentRequest) {
        try {
            return ResponseEntity.ok(studentService.registerStudent(registerStudentRequest));
        } catch (ValidationException exception) {
            return ResponseEntity.badRequest().body(validationErrors(exception));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/refresh/token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return authService.refreshToken(refreshTokenRequest.getRefreshToken())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(401).build());
    }

    @PutMapping(value = "/upload/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Student> uploadPhoto(
        @RequestAttribute("studentId") Long studentId,
        @RequestPart("photo") MultipartFile photo
    ) throws IOException {
        try {
            return studentService.uploadPhoto(studentId, photo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateStudent(@RequestAttribute("studentId") Long studentId, @RequestBody Student studentDetails) {
        if(studentDetails.getId() != null && !Objects.equals(studentId, studentDetails.getId()))
        {
            return ResponseEntity.notFound().build();
        }

        studentDetails.setId(studentId);
        try {
            return studentService.updateStudent(studentDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (ValidationException exception) {
            return ResponseEntity.badRequest().body(validationErrors(exception));
        }
    }

    private Map<String, List<String>> validationErrors(ValidationException exception) {
        return Map.of("errors", exception.getErrors());
    }
}
