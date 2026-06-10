package com.example.demo.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dtos.RegisterStudentRequest;
import com.example.demo.models.Student;
import com.example.demo.repositories.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final Path studentPhotosDirectory;

    public StudentService(
        StudentRepository studentRepository,
        @Value("${app.upload.student-photos-dir}") String studentPhotosDirectory
    ) {
        this.studentRepository = studentRepository;
        this.studentPhotosDirectory = Path.of(studentPhotosDirectory);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Optional<Student> getStudentByEmail(String email) {
        return studentRepository.findByEmail(email);
    }

    public Student registerStudent(RegisterStudentRequest registerStudentRequest) {
        String email = registerStudentRequest.getEmail().toLowerCase().trim();
        String passwordSalt = BCrypt.gensalt();
        String passwordHash = BCrypt.hashpw(registerStudentRequest.getPassword(), passwordSalt);
        Student student = new Student(
            null,
            null,
            registerStudentRequest.getName(),
            registerStudentRequest.getPhone(),
            email
        );

        student.setId(null);
        return studentRepository.save(student, passwordHash, passwordSalt);
    }

    public Optional<Student> updateStudent(Long id, Student studentDetails) {
        boolean updated = studentRepository.update(id, studentDetails);
        if (!updated) {
            return Optional.empty();
        }

        return studentRepository.findById(id);
    }

    public boolean deleteStudent(Long id) {
        return studentRepository.deleteById(id);
    }

    public Optional<Student> uploadPhoto(Long id, MultipartFile photo) throws IOException {
        Optional<Student> student = studentRepository.findById(id);
        if (student.isEmpty()) {
            return Optional.empty();
        }

        Files.createDirectories(studentPhotosDirectory);
        String photoFileName = id.toString();
        Path photoPath = studentPhotosDirectory.resolve(photoFileName);
        Files.copy(photo.getInputStream(), photoPath, StandardCopyOption.REPLACE_EXISTING);

        studentRepository.updatePhotoFileName(id, photoFileName);
        return studentRepository.findById(id);
    }
}
