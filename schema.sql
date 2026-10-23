-- ============================================================
-- Student Attendance Management System - Database Schema
-- Compatible with SQLite and MySQL / PostgreSQL
-- ============================================================

-- 1. Students Table
CREATE TABLE IF NOT EXISTS students (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- INT AUTO_INCREMENT for MySQL
    roll_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    course VARCHAR(50),
    semester VARCHAR(20),
    batch VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Subjects Table
CREATE TABLE IF NOT EXISTS subjects (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- INT AUTO_INCREMENT for MySQL
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    instructor VARCHAR(100)
);

-- 3. Attendance Table
CREATE TABLE IF NOT EXISTS attendance (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- INT AUTO_INCREMENT for MySQL
    student_id INTEGER NOT NULL,
    subject_code VARCHAR(30) NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(15) NOT NULL, -- 'PRESENT', 'ABSENT', 'LATE'
    remarks VARCHAR(255),
    CONSTRAINT uk_student_subj_date UNIQUE (student_id, subject_code, attendance_date),
    CONSTRAINT fk_att_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

-- Initial Subject Data
INSERT INTO subjects (code, name, instructor) VALUES 
('CS101', 'Core Java Programming', 'Dr. Sharma'),
('CS102', 'Data Structures & Algorithms', 'Prof. Verma'),
('CS103', 'Database Management Systems', 'Dr. Gupta'),
('CS104', 'Web Technologies', 'Prof. Singh');
