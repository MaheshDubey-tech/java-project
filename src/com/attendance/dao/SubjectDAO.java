package com.attendance.dao;

import com.attendance.model.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Subject operations using JDBC.
 */
public class SubjectDAO {

    public boolean addSubject(Subject subject) throws SQLException {
        String sql = "INSERT INTO subjects (code, name, instructor) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subject.getCode().toUpperCase().trim());
            pstmt.setString(2, subject.getName().trim());
            pstmt.setString(3, subject.getInstructor() != null ? subject.getInstructor().trim() : "");
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Subject> getAllSubjects() throws SQLException {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT id, code, name, instructor FROM subjects ORDER BY code ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Subject(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("instructor")
                ));
            }
        }
        return list;
    }

    public Subject getSubjectByCode(String code) throws SQLException {
        String sql = "SELECT id, code, name, instructor FROM subjects WHERE UPPER(code) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code.toUpperCase().trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Subject(
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getString("name"),
                            rs.getString("instructor")
                    );
                }
            }
        }
        return null;
    }

    public boolean subjectExists(String code) throws SQLException {
        return getSubjectByCode(code) != null;
    }
}
