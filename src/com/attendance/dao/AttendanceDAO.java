package com.attendance.dao;

import com.attendance.model.AttendanceRecord;
import com.attendance.model.AttendanceReport;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Attendance operations using JDBC.
 */
public class AttendanceDAO {

    /**
     * Records or updates attendance for a student on a specific date and subject.
     * Uses UPSERT logic (insert or update on conflict).
     */
    public boolean markAttendance(int studentId, String subjectCode, LocalDate date, String status, String remarks) throws SQLException {
        String dbType = DBConnection.getDbType();
        String sql;

        if ("mysql".equalsIgnoreCase(dbType)) {
            sql = "INSERT INTO attendance (student_id, subject_code, attendance_date, status, remarks) " +
                  "VALUES (?, ?, ?, ?, ?) " +
                  "ON DUPLICATE KEY UPDATE status = VALUES(status), remarks = VALUES(remarks)";
        } else {
            // SQLite
            sql = "INSERT INTO attendance (student_id, subject_code, attendance_date, status, remarks) " +
                  "VALUES (?, ?, ?, ?, ?) " +
                  "ON CONFLICT(student_id, subject_code, attendance_date) DO UPDATE SET " +
                  "status = excluded.status, remarks = excluded.remarks";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setString(2, subjectCode.toUpperCase().trim());
            pstmt.setString(3, date.toString());
            pstmt.setString(4, status.toUpperCase().trim());
            pstmt.setString(5, remarks != null ? remarks.trim() : "");

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all attendance records for a given date and subject.
     */
    public List<AttendanceRecord> getAttendanceByDateAndSubject(LocalDate date, String subjectCode) throws SQLException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT a.id, a.student_id, s.roll_no, s.name, a.subject_code, a.attendance_date, a.status, a.remarks " +
                     "FROM attendance a " +
                     "JOIN students s ON a.student_id = s.id " +
                     "WHERE a.attendance_date = ? AND UPPER(a.subject_code) = ? " +
                     "ORDER BY s.roll_no ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date.toString());
            pstmt.setString(2, subjectCode.toUpperCase().trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new AttendanceRecord(
                            rs.getInt("id"),
                            rs.getInt("student_id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("subject_code"),
                            LocalDate.parse(rs.getString("attendance_date")),
                            rs.getString("status"),
                            rs.getString("remarks")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves attendance history for a specific student.
     */
    public List<AttendanceRecord> getAttendanceByStudent(int studentId) throws SQLException {
        List<AttendanceRecord> list = new ArrayList<>();
        String sql = "SELECT a.id, a.student_id, s.roll_no, s.name, a.subject_code, a.attendance_date, a.status, a.remarks " +
                     "FROM attendance a " +
                     "JOIN students s ON a.student_id = s.id " +
                     "WHERE a.student_id = ? " +
                     "ORDER BY a.attendance_date DESC, a.subject_code ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new AttendanceRecord(
                            rs.getInt("id"),
                            rs.getInt("student_id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("subject_code"),
                            LocalDate.parse(rs.getString("attendance_date")),
                            rs.getString("status"),
                            rs.getString("remarks")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Generates aggregated summary reports across students, optionally filtered by subject.
     */
    public List<AttendanceReport> getAttendanceReport(String subjectCodeFilter) throws SQLException {
        List<AttendanceReport> list = new ArrayList<>();
        boolean hasFilter = (subjectCodeFilter != null && !subjectCodeFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(subjectCodeFilter.trim()));

        StringBuilder sql = new StringBuilder("""
            SELECT 
                s.id AS student_id,
                s.roll_no,
                s.name,
                s.course,
                COALESCE(a.subject_code, 'ALL') AS subject_code,
                COUNT(a.id) AS total_classes,
                SUM(CASE WHEN UPPER(a.status) = 'PRESENT' THEN 1 ELSE 0 END) AS present_count,
                SUM(CASE WHEN UPPER(a.status) = 'ABSENT' THEN 1 ELSE 0 END) AS absent_count,
                SUM(CASE WHEN UPPER(a.status) = 'LATE' THEN 1 ELSE 0 END) AS late_count
            FROM students s
            LEFT JOIN attendance a ON s.id = a.student_id
        """);

        if (hasFilter) {
            sql.append(" AND UPPER(a.subject_code) = ? ");
        }

        sql.append("""
            GROUP BY s.id, s.roll_no, s.name, s.course
            ORDER BY s.roll_no ASC
        """);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            if (hasFilter) {
                pstmt.setString(1, subjectCodeFilter.toUpperCase().trim());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new AttendanceReport(
                            rs.getInt("student_id"),
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getString("course"),
                            hasFilter ? subjectCodeFilter.toUpperCase().trim() : "ALL",
                            rs.getInt("total_classes"),
                            rs.getInt("present_count"),
                            rs.getInt("absent_count"),
                            rs.getInt("late_count")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Deletes an attendance record by record ID.
     */
    public boolean deleteAttendance(int recordId) throws SQLException {
        String sql = "DELETE FROM attendance WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recordId);
            return pstmt.executeUpdate() > 0;
        }
    }
}
