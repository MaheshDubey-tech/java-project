package com.attendance.model;

import java.time.LocalDate;

/**
 * Model representing an individual student attendance record.
 */
public class AttendanceRecord {
    private int id;
    private int studentId;
    private String studentRollNo;
    private String studentName;
    private String subjectCode;
    private LocalDate attendanceDate;
    private String status; // PRESENT, ABSENT, LATE
    private String remarks;

    public AttendanceRecord() {
    }

    public AttendanceRecord(int studentId, String subjectCode, LocalDate attendanceDate, String status, String remarks) {
        this.studentId = studentId;
        this.subjectCode = subjectCode;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.remarks = remarks;
    }

    public AttendanceRecord(int id, int studentId, String studentRollNo, String studentName,
                            String subjectCode, LocalDate attendanceDate, String status, String remarks) {
        this.id = id;
        this.studentId = studentId;
        this.studentRollNo = studentRollNo;
        this.studentName = studentName;
        this.subjectCode = subjectCode;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.remarks = remarks;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentRollNo() {
        return studentRollNo;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return String.format("AttendanceRecord [ID: %d | Roll: %s | Name: %s | Subject: %s | Date: %s | Status: %s]",
                id, studentRollNo, studentName, subjectCode, attendanceDate, status);
    }
}
