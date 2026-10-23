package com.attendance.model;

import java.sql.Timestamp;

/**
 * Model representing a Student.
 */
public class Student {
    private int id;
    private String rollNo;
    private String name;
    private String email;
    private String phone;
    private String course;
    private String semester;
    private String batch;
    private Timestamp createdAt;

    public Student() {
    }

    public Student(String rollNo, String name, String email, String phone, String course, String semester, String batch) {
        this.rollNo = rollNo;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.course = course;
        this.semester = semester;
        this.batch = batch;
    }

    public Student(int id, String rollNo, String name, String email, String phone, String course, String semester, String batch, Timestamp createdAt) {
        this.id = id;
        this.rollNo = rollNo;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.course = course;
        this.semester = semester;
        this.batch = batch;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("Student [ID: %d | Roll No: %s | Name: %s | Course: %s | Sem: %s | Batch: %s]",
                id, rollNo, name, course, semester, batch);
    }
}
