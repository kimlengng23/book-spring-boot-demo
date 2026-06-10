package com.example.demo.dtos;

public class StudentAuthData {

    private Long studentId;
    private String studentNumber;
    private String passwordHash;
    private String passwordSalt;

    public StudentAuthData(Long studentId, String studentNumber, String passwordHash, String passwordSalt) {
        this.studentId = studentId;
        this.studentNumber = studentNumber;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }
}
