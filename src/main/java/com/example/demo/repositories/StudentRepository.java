package com.example.demo.repositories;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.demo.dtos.StudentAuthData;
import com.example.demo.models.Student;

@Repository
public class StudentRepository {

    private final JdbcTemplate jdbcTemplate;

    public StudentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Student> getStudentById(Long id) {
        String sql = """
            SELECT id, studentNumber, photoFileName, name, phone, email, isVerified, isActive
            FROM Students
            WHERE id = ? AND isActive = TRUE
        """;

        try {
            Student student = jdbcTemplate.queryForObject(sql, this::mapRowToStudent, id);
            return Optional.of(student);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<Student> getStudentByNumber(String studentNumber) {
        String sql = """
            SELECT id, studentNumber, photoFileName, name, phone, email, isVerified, isActive
            FROM Students
            WHERE studentNumber = ? AND isActive = TRUE
        """;

        try {
            Student student = jdbcTemplate.queryForObject(sql, this::mapRowToStudent, studentNumber.trim());
            return Optional.of(student);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<StudentAuthData> getAuthDataByStudentNumber(String studentNumber) {
        String sql = """
            SELECT id, studentNumber, passwordHash, passwordSalt
            FROM Students
            WHERE studentNumber = ? AND isActive = TRUE
            """;

        try {
            StudentAuthData authData = jdbcTemplate.queryForObject(sql, (resultSet, rowNumber) -> new StudentAuthData(
                resultSet.getLong("id"),
                resultSet.getString("studentNumber"),
                resultSet.getString("passwordHash"),
                resultSet.getString("passwordSalt")
            ), studentNumber.trim());
            return Optional.of(authData);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public boolean existsStudentByNumber(String studentNumber) {
        String sql = """
            SELECT COUNT(*)
            FROM Students
            WHERE studentNumber = ?
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentNumber.trim());
        return count != null && count > 0;
    }

    public boolean existsStudentByEmail(String email) {
        String sql = """
            SELECT COUNT(*)
            FROM Students
            WHERE LOWER(email) = LOWER(?)
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email.trim());
        return count != null && count > 0;
    }

    public boolean existsStudentByPhone(String phone) {
        String sql = """
            SELECT COUNT(*)
            FROM Students
            WHERE phone = ?
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, phone.trim());
        return count != null && count > 0;
    }

    public boolean existsStudentByEmailExceptId(String email, Long id) {
        String sql = """
            SELECT COUNT(*)
            FROM Students
            WHERE LOWER(email) = LOWER(?) AND id <> ?
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email.trim(), id);
        return count != null && count > 0;
    }

    public boolean existsStudentByPhoneExceptId(String phone, Long id) {
        String sql = """
            SELECT COUNT(*)
            FROM Students
            WHERE phone = ? AND id <> ?
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, phone.trim(), id);
        return count != null && count > 0;
    }

    public Student createStudent(Student student, String passwordHash, String passwordSalt) {
        String sql = """
            INSERT INTO Students (studentNumber, photoFileName, name, phone, email, passwordHash, passwordSalt, isVerified, isActive)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, student.getStudentNumber().trim());
            statement.setString(2, student.getPhotoFileName());
            statement.setString(3, student.getName());
            statement.setString(4, student.getPhone().trim());
            statement.setString(5, student.getEmail().toLowerCase().trim());
            statement.setString(6, passwordHash);
            statement.setString(7, passwordSalt);
            statement.setBoolean(8, getVerifiedValue(student.getIsVerified()));
            statement.setBoolean(9, getActiveValue(student.getIsActive()));
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            student.setId(generatedId.longValue());
        }

        return student;
    }

    public boolean updateStudent(Student student) {
        String sql = """
            UPDATE Students
            SET name = ?, phone = ?, email = ?, isActive = COALESCE(?, isActive)
            WHERE id = ? AND isActive = TRUE
            """;

        int updatedRows = jdbcTemplate.update(
            sql,
            student.getName(),
            student.getPhone().trim(),
            student.getEmail().toLowerCase().trim(),
            student.getIsActive(),
            student.getId()
        );

        return updatedRows > 0;
    }

    public boolean updatePhotoFileName(Long id, String photoFileName) {
        String sql = """
            UPDATE Students
            SET photoFileName = ?
            WHERE id = ? AND isActive = TRUE
            """;

        int updatedRows = jdbcTemplate.update(sql, photoFileName, id);
        return updatedRows > 0;
    }

    private Student mapRowToStudent(java.sql.ResultSet resultSet, int rowNumber) throws java.sql.SQLException {
        return new Student(
            resultSet.getLong("id"),
            resultSet.getString("studentNumber"),
            resultSet.getString("photoFileName"),
            resultSet.getString("name"),
            resultSet.getString("phone"),
            resultSet.getString("email"),
            resultSet.getBoolean("isVerified"),
            resultSet.getBoolean("isActive")
        );
    }

    private boolean getVerifiedValue(Boolean isVerified) {
        return isVerified != null && isVerified;
    }

    private boolean getActiveValue(Boolean isActive) {
        return isActive == null || isActive;
    }
}
