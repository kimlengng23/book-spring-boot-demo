package com.example.demo.dtos;

public class StudentAuthData {

    private Long studentId;
    private String passwordHash;
    private String passwordSalt;

    public StudentAuthData(Long studentId, String passwordHash, String passwordSalt) {
        this.studentId = studentId;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }
}
