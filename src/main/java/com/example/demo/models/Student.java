package com.example.demo.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

//Student: ID, student number, 1 portrait photo, name, phone number, email.
@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String studentNumber;
    private String photoFileName;
    private String name;
    private String phone;
    private String email;
    private String passwordHash;
    private String passwordSalt;
    private Boolean isVerified;
    private Boolean isActive;

    public Student() {
    }

    public Student(
        Long id,
        String studentNumber,
        String photoFileName, 
        String name, 
        String phone, 
        String email,
        Boolean isVerified,
        Boolean isActive) {
        this.id = id;
        this.studentNumber = studentNumber;
        this.photoFileName = photoFileName;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.isVerified = isVerified;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getPhotoFileName() {
        return photoFileName;
    }

    public void setPhotoFileName(String photoFileName) {
        this.photoFileName = photoFileName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
