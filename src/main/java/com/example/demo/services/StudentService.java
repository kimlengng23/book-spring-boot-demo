package com.example.demo.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dtos.RegisterStudentRequest;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.models.Student;
import com.example.demo.repositories.StudentRepository;

@Service
public class StudentService {

    private static final Set<String> ALLOWED_PHOTO_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png"
    );
    private static final Pattern STUDENT_NUMBER_PATTERN = Pattern.compile("^\\d{3,}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{9,10}$");

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

    public Optional<Student> getStudentByNumber(String studentNumber) {
        return studentRepository.getStudentByNumber(studentNumber);
    }

    public Student registerStudent(RegisterStudentRequest registerStudentRequest) {
        validateRegisterStudent(registerStudentRequest);

        String email = registerStudentRequest.getEmail().toLowerCase().trim();
        String passwordSalt = BCrypt.gensalt();
        String passwordHash = BCrypt.hashpw(registerStudentRequest.getPassword(), passwordSalt);
        Student student = new Student(
            null,
            registerStudentRequest.getStudentNumber().trim(),
            null,
            registerStudentRequest.getName(),
            registerStudentRequest.getPhone().trim(),
            email,
            false,
            true
        );

        student.setId(null);
        try {
            return studentRepository.createStudent(student, passwordHash, passwordSalt);
        } catch (DuplicateKeyException exception) {
            throw new ValidationException(List.of("Student number, email, or phone is already registered."));
        }
    }

    public Optional<Student> updateStudent(Student studentDetails) {
        validateUpdateStudent(studentDetails);

        boolean updated;
        try {
            updated = studentRepository.updateStudent(studentDetails);
        } catch (DuplicateKeyException exception) {
            throw new ValidationException(List.of("Email or phone is already registered."));
        }

        if (!updated) {
            return Optional.empty();
        }

        return studentRepository.getStudentById(studentDetails.getId());
    }

    private void validateRegisterStudent(RegisterStudentRequest registerStudentRequest) {
        List<String> errors = new ArrayList<>();

        validateStudentNumber(registerStudentRequest.getStudentNumber(), true, errors);
        validateStudentNumberIsAvailable(registerStudentRequest.getStudentNumber(), errors);
        validateEmail(registerStudentRequest.getEmail(), errors);
        validateEmailIsAvailable(registerStudentRequest.getEmail(), errors);
        validatePhone(registerStudentRequest.getPhone(), errors);
        validatePhoneIsAvailable(registerStudentRequest.getPhone(), errors);
        validatePassword(registerStudentRequest.getPassword(), errors);

        throwIfInvalid(errors);
    }

    private void validateUpdateStudent(Student studentDetails) {
        List<String> errors = new ArrayList<>();

        validateStudentNumber(studentDetails.getStudentNumber(), false, errors);
        validateEmail(studentDetails.getEmail(), errors);
        validateEmailIsAvailableForUpdate(studentDetails.getEmail(), studentDetails.getId(), errors);
        validatePhone(studentDetails.getPhone(), errors);
        validatePhoneIsAvailableForUpdate(studentDetails.getPhone(), studentDetails.getId(), errors);

        throwIfInvalid(errors);
    }

    private void validateStudentNumber(String studentNumber, boolean required, List<String> errors) {
        if (isBlank(studentNumber)) {
            if (required) {
                errors.add("Student number is required.");
            }
            return;
        }

        if (!STUDENT_NUMBER_PATTERN.matcher(studentNumber.trim()).matches()) {
            errors.add("Student number must be at least 3 digits long.");
        }
    }

    private void validateStudentNumberIsAvailable(String studentNumber, List<String> errors) {
        if (isBlank(studentNumber) || !STUDENT_NUMBER_PATTERN.matcher(studentNumber.trim()).matches()) {
            return;
        }

        if (studentRepository.existsStudentByNumber(studentNumber)) {
            errors.add("Student number is already registered.");
        }
    }

    private void validateEmail(String email, List<String> errors) {
        if (isBlank(email)) {
            errors.add("Email is required.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errors.add("Email must be in a valid format.");
        }
    }

    private void validateEmailIsAvailable(String email, List<String> errors) {
        if (isBlank(email) || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return;
        }

        if (studentRepository.existsStudentByEmail(email)) {
            errors.add("Email is already registered.");
        }
    }

    private void validateEmailIsAvailableForUpdate(String email, Long studentId, List<String> errors) {
        if (studentId == null || isBlank(email) || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return;
        }

        if (studentRepository.existsStudentByEmailExceptId(email, studentId)) {
            errors.add("Email is already registered.");
        }
    }

    private void validatePhone(String phone, List<String> errors) {
        if (isBlank(phone)) {
            errors.add("Phone is required.");
            return;
        }

        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            errors.add("Phone must be 9 to 10 digits.");
        }
    }

    private void validatePhoneIsAvailable(String phone, List<String> errors) {
        if (isBlank(phone) || !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return;
        }

        if (studentRepository.existsStudentByPhone(phone)) {
            errors.add("Phone is already registered.");
        }
    }

    private void validatePhoneIsAvailableForUpdate(String phone, Long studentId, List<String> errors) {
        if (studentId == null || isBlank(phone) || !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return;
        }

        if (studentRepository.existsStudentByPhoneExceptId(phone, studentId)) {
            errors.add("Phone is already registered.");
        }
    }

    private void validatePassword(String password, List<String> errors) {
        if (isBlank(password)) {
            errors.add("Password is required.");
            return;
        }

        if (password.length() < 6) {
            errors.add("Password must be at least 6 characters long.");
        }
    }

    private void throwIfInvalid(List<String> errors) {
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
