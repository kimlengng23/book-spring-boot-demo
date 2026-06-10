package com.example.demo.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dtos.RegisterStudentRequest;
import com.example.demo.models.Student;
import com.example.demo.repositories.StudentRepository;

@Service
public class StudentService {

    private static final Set<String> ALLOWED_PHOTO_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png"
    );

    private final StudentRepository studentRepository;
    private final Path studentPhotosDirectory;

    public StudentService(
        StudentRepository studentRepository,
        @Value("${app.upload.student-photos-dir}") String studentPhotosDirectory
    ) {
        this.studentRepository = studentRepository;
        this.studentPhotosDirectory = Path.of(studentPhotosDirectory);
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.getStudentById(id);
    }

    public Optional<Student> getStudentByEmail(String email) {
        return studentRepository.getStudentByEmail(email);
    }

    public Student registerStudent(RegisterStudentRequest registerStudentRequest) {
        String email = registerStudentRequest.getEmail().toLowerCase().trim();
        String passwordSalt = BCrypt.gensalt();
        String passwordHash = BCrypt.hashpw(registerStudentRequest.getPassword(), passwordSalt);
        Student student = new Student(
            null,
            registerStudentRequest.getStudentNumber().trim(),
            null,
            registerStudentRequest.getName(),
            registerStudentRequest.getPhone(),
            email,
            true
        );

        student.setId(null);
        return studentRepository.createStudent(student, passwordHash, passwordSalt);
    }

    public Optional<Student> updateStudent(Student studentDetails) {
        boolean updated = studentRepository.updateStudent(studentDetails);
        if (!updated) {
            return Optional.empty();
        }

        return studentRepository.getStudentById(studentDetails.getId());
    }

    public Optional<Student> uploadPhoto(Long id, MultipartFile photo) throws IOException {
        Optional<Student> student = studentRepository.getStudentById(id);
        if (student.isEmpty()) {
            return Optional.empty();
        }

        validatePhoto(photo);

        Files.createDirectories(studentPhotosDirectory);
        String photoFileName = UUID.randomUUID() + getPhotoExtension(photo);
        Path photoPath = studentPhotosDirectory.resolve(photoFileName);
        Files.copy(photo.getInputStream(), photoPath, StandardCopyOption.REPLACE_EXISTING);

        studentRepository.updatePhotoFileName(id, photoFileName);
        return studentRepository.getStudentById(id);
    }

    private void validatePhoto(MultipartFile photo) throws IOException {
        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException("Photo is required");
        }

        String contentType = photo.getContentType();
        if (contentType == null || !ALLOWED_PHOTO_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Only JPEG and PNG images are allowed");
        }

        if (ImageIO.read(photo.getInputStream()) == null) {
            throw new IllegalArgumentException("Uploaded file is not a valid image");
        }
    }

    private String getPhotoExtension(MultipartFile photo) {
        String contentType = photo.getContentType();
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }

        return ".jpg";
    }
}
