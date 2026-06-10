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
            SELECT id, studentNumber, photoFileName, name, phone, email, isActive
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

    public Optional<Student> getStudentByEmail(String email) {
        String sql = """
            SELECT id, studentNumber, photoFileName, name, phone, email, isActive
            FROM Students
            WHERE email = ? AND isActive = TRUE
        """;

        try {
            Student student = jdbcTemplate.queryForObject(sql, this::mapRowToStudent, email.toLowerCase().trim());
            return Optional.of(student);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<StudentAuthData> getAuthDataByStudentNumber(String studentNumber) {
        String sql = """
            SELECT id, passwordHash, passwordSalt
            FROM Students
            WHERE studentNumber = ? AND isActive = TRUE
            """;

        try {
            StudentAuthData authData = jdbcTemplate.queryForObject(sql, (resultSet, rowNumber) -> new StudentAuthData(
                resultSet.getLong("id"),
                resultSet.getString("passwordHash"),
                resultSet.getString("passwordSalt")
            ), studentNumber.trim());
            return Optional.of(authData);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Student createStudent(Student student, String passwordHash, String passwordSalt) {
        String sql = """
            INSERT INTO Students (studentNumber, photoFileName, name, phone, email, passwordHash, passwordSalt, isActive)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, student.getStudentNumber().trim());
            statement.setString(2, student.getPhotoFileName());
            statement.setString(3, student.getName());
            statement.setString(4, student.getPhone());
            statement.setString(5, student.getEmail().toLowerCase().trim());
            statement.setString(6, passwordHash);
            statement.setString(7, passwordSalt);
            statement.setBoolean(8, getActiveValue(student.getIsActive()));
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
            SET photoFileName = ?, name = ?, phone = ?, email = ?, isActive = COALESCE(?, isActive)
            WHERE id = ? AND isActive = TRUE
            """;

        int updatedRows = jdbcTemplate.update(
            sql,
            student.getPhotoFileName(),
            student.getName(),
            student.getPhone(),
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
            resultSet.getBoolean("isActive")
        );
    }

    private boolean getActiveValue(Boolean isActive) {
        return isActive == null || isActive;
    }
}
