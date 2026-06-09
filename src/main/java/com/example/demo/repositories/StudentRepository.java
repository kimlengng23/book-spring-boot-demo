package com.example.demo.repositories;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

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

    @PostConstruct
    public void createStudentTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS student (
                id BIGINT NOT NULL AUTO_INCREMENT,
                photo_file_name VARCHAR(255),
                name VARCHAR(255) NOT NULL,
                phone VARCHAR(50),
                email VARCHAR(255) NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                password_salt VARCHAR(255) NOT NULL,
                PRIMARY KEY (id),
                UNIQUE KEY uk_student_email (email)
            ) ENGINE=InnoDB
            """;

        jdbcTemplate.execute(sql);

        String addPhotoFileNameSql = """
            ALTER TABLE student
            ADD COLUMN IF NOT EXISTS photo_file_name VARCHAR(255)
            """;
        jdbcTemplate.execute(addPhotoFileNameSql);

        String addPasswordHashSql = """
            ALTER TABLE student
            ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255) NOT NULL DEFAULT ''
            """;
        jdbcTemplate.execute(addPasswordHashSql);

        String addPasswordSaltSql = """
            ALTER TABLE student
            ADD COLUMN IF NOT EXISTS password_salt VARCHAR(255) NOT NULL DEFAULT ''
            """;
        jdbcTemplate.execute(addPasswordSaltSql);
    }

    public List<Student> findAll() {
        String sql = """
            SELECT id, photo_file_name, name, phone, email
            FROM student
            ORDER BY id
            """;

        return jdbcTemplate.query(sql, this::mapRowToStudent);
    }

    public Optional<Student> findById(Long id) {
        String sql = """
            SELECT id, photo_file_name, name, phone, email
            FROM student
            WHERE id = ?
            """;

        try {
            Student student = jdbcTemplate.queryForObject(sql, this::mapRowToStudent, id);
            return Optional.of(student);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<Student> findByEmail(String email) {
        String sql = """
            SELECT id, photo_file_name, name, phone, email
            FROM student
            WHERE email = ?
            """;

        try {
            Student student = jdbcTemplate.queryForObject(sql, this::mapRowToStudent, email.toLowerCase().trim());
            return Optional.of(student);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<StudentAuthData> findAuthDataByEmail(String email) {
        String sql = """
            SELECT id, password_hash, password_salt
            FROM student
            WHERE email = ?
            """;

        try {
            StudentAuthData authData = jdbcTemplate.queryForObject(sql, (resultSet, rowNumber) -> new StudentAuthData(
                resultSet.getLong("id"),
                resultSet.getString("password_hash"),
                resultSet.getString("password_salt")
            ), email.toLowerCase().trim());
            return Optional.of(authData);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Student save(Student student, String passwordHash, String passwordSalt) {
        String sql = """
            INSERT INTO student (photo_file_name, name, phone, email, password_hash, password_salt)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, student.getPhotoFileName());
            statement.setString(2, student.getName());
            statement.setString(3, student.getPhone());
            statement.setString(4, student.getEmail().toLowerCase().trim());
            statement.setString(5, passwordHash);
            statement.setString(6, passwordSalt);
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            student.setId(generatedId.longValue());
        }

        return student;
    }

    public boolean update(Long id, Student student) {
        String sql = """
            UPDATE student
            SET photo_file_name = ?, name = ?, phone = ?, email = ?
            WHERE id = ?
            """;

        int updatedRows = jdbcTemplate.update(
            sql,
            student.getPhotoFileName(),
            student.getName(),
            student.getPhone(),
            student.getEmail().toLowerCase().trim(),
            id
        );

        return updatedRows > 0;
    }

    public boolean updatePhotoFileName(Long id, String photoFileName) {
        String sql = """
            UPDATE student
            SET photo_file_name = ?
            WHERE id = ?
            """;

        int updatedRows = jdbcTemplate.update(sql, photoFileName, id);
        return updatedRows > 0;
    }

    public boolean deleteById(Long id) {
        String sql = """
            DELETE FROM student
            WHERE id = ?
            """;

        int deletedRows = jdbcTemplate.update(sql, id);
        return deletedRows > 0;
    }

    private Student mapRowToStudent(java.sql.ResultSet resultSet, int rowNumber) throws java.sql.SQLException {
        return new Student(
            resultSet.getLong("id"),
            resultSet.getString("photo_file_name"),
            resultSet.getString("name"),
            resultSet.getString("phone"),
            resultSet.getString("email")
        );
    }
}
