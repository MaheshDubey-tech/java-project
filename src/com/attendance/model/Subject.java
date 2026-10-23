package com.attendance.model;

/**
 * Model representing a Subject/Course unit.
 */
public class Subject {
    private int id;
    private String code;
    private String name;
    private String instructor;

    public Subject() {
    }

    public Subject(String code, String name, String instructor) {
        this.code = code;
        this.name = name;
        this.instructor = instructor;
    }

    public Subject(int id, String code, String name, String instructor) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.instructor = instructor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (Instructor: %s)", code, name, instructor);
    }
}
