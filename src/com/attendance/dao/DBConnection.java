package com.attendance.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Manages database connectivity and initial schema creation using JDBC.
 * Supports SQLite (zero-config local embedded file) and MySQL (configurable via db.properties).
 */
public class DBConnection {
    private static final Properties properties = new Properties();
    private static String dbType = "sqlite";
    private static String dbUrl = "jdbc:sqlite:attendance.db";
    private static String dbUser = "";
    private static String dbPassword = "";

    static {
        loadConfiguration();
        initializeDatabase();
    }

    private static void loadConfiguration() {
        File configFile = new File("db.properties");
        if (configFile.exists()) {
            try (InputStream in = new FileInputStream(configFile)) {
                properties.load(in);
                dbType = properties.getProperty("db.type", "sqlite").trim().toLowerCase();
                if ("mysql".equals(dbType)) {
                    dbUrl = properties.getProperty("mysql.url", "jdbc:mysql://localhost:3306/attendance_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true");
                    dbUser = properties.getProperty("mysql.user", "root");
                    dbPassword = properties.getProperty("mysql.password", "");
                } else {
                    dbUrl = properties.getProperty("sqlite.url", "jdbc:sqlite:attendance.db");
                }
            } catch (Exception e) {
                System.err.println("Notice: Could not load db.properties, using SQLite default. (" + e.getMessage() + ")");
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            if ("mysql".equalsIgnoreCase(dbType)) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            } else {
                Class.forName("org.sqlite.JDBC");
                return DriverManager.getConnection(dbUrl);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver not found for " + dbType + ". Ensure driver JAR is in classpath: " + e.getMessage(), e);
        }
    }

    public static String getDbType() {
        return dbType;
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    /**
     * Initializes database tables automatically if they do not already exist.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            if ("mysql".equalsIgnoreCase(dbType)) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS students (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        roll_no VARCHAR(50) NOT NULL UNIQUE,
                        name VARCHAR(100) NOT NULL,
                        email VARCHAR(100),
                        phone VARCHAR(20),
                        course VARCHAR(50),
                        semester VARCHAR(20),
                        batch VARCHAR(20),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    ) ENGINE=InnoDB;
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS subjects (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        code VARCHAR(30) NOT NULL UNIQUE,
                        name VARCHAR(100) NOT NULL,
                        instructor VARCHAR(100)
                    ) ENGINE=InnoDB;
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS attendance (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        student_id INT NOT NULL,
                        subject_code VARCHAR(30) NOT NULL,
                        attendance_date DATE NOT NULL,
                        status VARCHAR(15) NOT NULL,
                        remarks VARCHAR(255),
                        CONSTRAINT uk_student_subj_date UNIQUE (student_id, subject_code, attendance_date),
                        CONSTRAINT fk_att_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
                    ) ENGINE=InnoDB;
                """);
            } else {
                // SQLite
                stmt.execute("PRAGMA foreign_keys = ON;");

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS students (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        roll_no TEXT NOT NULL UNIQUE,
                        name TEXT NOT NULL,
                        email TEXT,
                        phone TEXT,
                        course TEXT,
                        semester TEXT,
                        batch TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS subjects (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        code TEXT NOT NULL UNIQUE,
                        name TEXT NOT NULL,
                        instructor TEXT
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS attendance (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        student_id INTEGER NOT NULL,
                        subject_code TEXT NOT NULL,
                        attendance_date TEXT NOT NULL,
                        status TEXT NOT NULL,
                        remarks TEXT,
                        UNIQUE(student_id, subject_code, attendance_date),
                        FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
                    );
                """);
            }

            // Insert sample subjects and students if empty
            seedInitialData(conn);
        } catch (SQLException e) {
            System.err.println("Warning: Database initialization error: " + e.getMessage());
        }
    }

    private static void seedInitialData(Connection conn) {
        try (Statement checkStmt = conn.createStatement()) {
            var rs = checkStmt.executeQuery("SELECT COUNT(*) FROM subjects");
            if (rs.next() && rs.getInt(1) == 0) {
                try (Statement insertStmt = conn.createStatement()) {
                    insertStmt.execute("INSERT INTO subjects (code, name, instructor) VALUES ('CS101', 'Core Java Programming', 'Dr. Sharma');");
                    insertStmt.execute("INSERT INTO subjects (code, name, instructor) VALUES ('CS102', 'Data Structures & Algorithms', 'Prof. Verma');");
                    insertStmt.execute("INSERT INTO subjects (code, name, instructor) VALUES ('CS103', 'Database Management Systems', 'Dr. Gupta');");
                    insertStmt.execute("INSERT INTO subjects (code, name, instructor) VALUES ('CS104', 'Web Technologies', 'Prof. Singh');");
                }
            }

            var rsStudents = checkStmt.executeQuery("SELECT COUNT(*) FROM students");
            if (rsStudents.next() && rsStudents.getInt(1) == 0) {
                try (Statement insertStmt = conn.createStatement()) {
                    insertStmt.execute("INSERT INTO students (roll_no, name, email, phone, course, semester, batch) VALUES " +
                            "('CS202401', 'Aarav Patel', 'aarav.patel@college.edu', '9876543210', 'B.Tech CSE', 'Semester 4', '2024-2028')," +
                            "('CS202402', 'Diya Dubey', 'diya.dubey@college.edu', '9876543211', 'B.Tech CSE', 'Semester 4', '2024-2028')," +
                            "('CS202403', 'Rohan Mehta', 'rohan.mehta@college.edu', '9876543212', 'B.Tech CSE', 'Semester 4', '2024-2028')," +
                            "('CS202404', 'Ananya Iyer', 'ananya.iyer@college.edu', '9876543213', 'B.Tech CSE', 'Semester 4', '2024-2028')," +
                            "('CS202405', 'Kabir Verma', 'kabir.verma@college.edu', '9876543214', 'B.Tech CSE', 'Semester 4', '2024-2028');");

                    // Seed some initial attendance logs
                    String today = java.time.LocalDate.now().toString();
                    String yesterday = java.time.LocalDate.now().minusDays(1).toString();
                    String dayBefore = java.time.LocalDate.now().minusDays(2).toString();

                    insertStmt.execute("INSERT INTO attendance (student_id, subject_code, attendance_date, status, remarks) VALUES " +
                            "(1, 'CS101', '" + dayBefore + "', 'PRESENT', 'On time')," +
                            "(2, 'CS101', '" + dayBefore + "', 'PRESENT', 'On time')," +
                            "(3, 'CS101', '" + dayBefore + "', 'ABSENT', 'Medical Leave')," +
                            "(4, 'CS101', '" + dayBefore + "', 'PRESENT', 'On time')," +
                            "(5, 'CS101', '" + dayBefore + "', 'PRESENT', 'On time')," +
                            "(1, 'CS101', '" + yesterday + "', 'PRESENT', 'On time')," +
                            "(2, 'CS101', '" + yesterday + "', 'PRESENT', 'On time')," +
                            "(3, 'CS101', '" + yesterday + "', 'ABSENT', 'Unexcused')," +
                            "(4, 'CS101', '" + yesterday + "', 'LATE', '10 mins late')," +
                            "(5, 'CS101', '" + yesterday + "', 'PRESENT', 'On time')," +
                            "(1, 'CS101', '" + today + "', 'PRESENT', 'On time')," +
                            "(2, 'CS101', '" + today + "', 'PRESENT', 'On time')," +
                            "(3, 'CS101', '" + today + "', 'PRESENT', 'On time')," +
                            "(4, 'CS101', '" + today + "', 'PRESENT', 'On time')," +
                            "(5, 'CS101', '" + today + "', 'ABSENT', 'Sick');");
                }
            }
        } catch (Exception ignored) {
        }
    }
}
