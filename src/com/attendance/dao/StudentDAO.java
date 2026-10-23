package com.attendance.dao;

import com.attendance.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student CRUD operations using JDBC PreparedStatement.
 */
public class StudentDAO {

    public boolean addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (roll_no, name, email, phone, course, semester, batch) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, student.getRollNo().trim());
            pstmt.setString(2, student.getName().trim());
            pstmt.setString(3, student.getEmail() != null ? student.getEmail().trim() : "");
            pstmt.setString(4, student.getPhone() != null ? student.getPhone().trim() : "");
            pstmt.setString(5, student.getCourse() != null ? student.getCourse().trim() : "");
            pstmt.setString(6, student.getSemester() != null ? student.getSemester().trim() : "");
            pstmt.setString(7, student.getBatch() != null ? student.getBatch().trim() : "");

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean updateStudent(Student student) throws SQLException {
        String sql = "UPDATE students SET name = ?, email = ?, phone = ?, course = ?, semester = ?, batch = ? WHERE roll_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getName().trim());
            pstmt.setString(2, student.getEmail() != null ? student.getEmail().trim() : "");
            pstmt.setString(3, student.getPhone() != null ? student.getPhone().trim() : "");
            pstmt.setString(4, student.getCourse() != null ? student.getCourse().trim() : "");
            pstmt.setString(5, student.getSemester() != null ? student.getSemester().trim() : "");
            pstmt.setString(6, student.getBatch() != null ? student.getBatch().trim() : "");
            pstmt.setString(7, student.getRollNo().trim());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteStudentByRollNo(String rollNo) throws SQLException {
        String sql = "DELETE FROM students WHERE roll_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNo.trim());
            return pstmt.executeUpdate() > 0;
        }
    }

    public Student getStudentByRollNo(String rollNo) throws SQLException {
        String sql = "SELECT id, roll_no, name, email, phone, course, semester, batch, created_at FROM students WHERE roll_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNo.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStudent(rs);
                }
            }
        }
        return null;
    }

    public Student getStudentById(int id) throws SQLException {
        String sql = "SELECT id, roll_no, name, email, phone, course, semester, batch, created_at FROM students WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStudent(rs);
                }
            }
        }
        return null;
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT id, roll_no, name, email, phone, course, semester, batch, created_at FROM students ORDER BY roll_no ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToStudent(rs));
            }
        }
        return list;
    }

    public List<Student> searchStudents(String query) throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT id, roll_no, name, email, phone, course, semester, batch, created_at FROM students " +
                     "WHERE LOWER(roll_no) LIKE ? OR LOWER(name) LIKE ? OR LOWER(course) LIKE ? ORDER BY roll_no ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String wildcard = "%" + query.trim().toLowerCase() + "%";
            pstmt.setString(1, wildcard);
            pstmt.setString(2, wildcard);
            pstmt.setString(3, wildcard);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToStudent(rs));
                }
            }
        }
        return list;
    }

    public int getStudentCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM students";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Student mapRowToStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("id"),
                rs.getString("roll_no"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("course"),
                rs.getString("semester"),
                rs.getString("batch"),
                rs.getTimestamp("created_at")
        );
    }
}
