package com.attendance;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.DBConnection;
import com.attendance.dao.StudentDAO;
import com.attendance.dao.SubjectDAO;
import com.attendance.model.AttendanceRecord;
import com.attendance.model.AttendanceReport;
import com.attendance.model.Student;
import com.attendance.model.Subject;
import com.attendance.util.ConsoleUtils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Main entry point for the Attendance Management System.
 * Pure Java + JDBC application with interactive CLI.
 */
public class AttendanceApp {

    private static final StudentDAO studentDAO = new StudentDAO();
    private static final SubjectDAO subjectDAO = new SubjectDAO();
    private static final AttendanceDAO attendanceDAO = new AttendanceDAO();

    public static void main(String[] args) {
        // Welcome Banner
        System.out.println(ConsoleUtils.CYAN + """
         ===========================================================
         🎓  STUDENT ATTENDANCE MANAGEMENT SYSTEM  🎓
             Built with Pure Java & JDBC (PreparedStatements)
         ===========================================================
        """ + ConsoleUtils.RESET);

        ConsoleUtils.printInfo("Database Mode: " + DBConnection.getDbType().toUpperCase() + " (" + DBConnection.getDbUrl() + ")");

        boolean exit = false;
        while (!exit) {
            printMainMenu();
            int choice = ConsoleUtils.readIntInRange("Enter your choice [1-6]: ", 1, 6);

            switch (choice) {
                case 1 -> studentMenu();
                case 2 -> attendanceMenu();
                case 3 -> reportsMenu();
                case 4 -> subjectMenu();
                case 5 -> showDatabaseInfo();
                case 6 -> {
                    exit = true;
                    ConsoleUtils.printSuccess("Thank you for using the Attendance Management System! Goodbye.");
                }
            }
        }
    }

    private static void printMainMenu() {
        ConsoleUtils.printHeader("MAIN MENU");
        System.out.println("  1. Student Management (Add, View, Update, Delete)");
        System.out.println("  2. Attendance Marking & Logs");
        System.out.println("  3. Reports & Defaulter Tracking");
        System.out.println("  4. Subject Management");
        System.out.println("  5. Database Information");
        System.out.println("  6. Exit");
        System.out.println("───────────────────────────────────────────────────────────");
    }

    // ==========================================
    //  STUDENT MANAGEMENT
    // ==========================================

    private static void studentMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("STUDENT MANAGEMENT");
            System.out.println("  1. Add New Student");
            System.out.println("  2. View All Students");
            System.out.println("  3. Search Student (by Roll No / Name)");
            System.out.println("  4. Update Student Details");
            System.out.println("  5. Delete Student");
            System.out.println("  6. Back to Main Menu");
            System.out.println("───────────────────────────────────────────────────────────");

            int choice = ConsoleUtils.readIntInRange("Select an option [1-6]: ", 1, 6);
            switch (choice) {
                case 1 -> addNewStudent();
                case 2 -> viewAllStudents();
                case 3 -> searchStudent();
                case 4 -> updateStudent();
                case 5 -> deleteStudent();
                case 6 -> back = true;
            }
        }
    }

    private static void addNewStudent() {
        ConsoleUtils.printSubHeader("Add New Student");
        String rollNo = ConsoleUtils.readNonEmptyString("Enter Roll Number (e.g., CS2024001): ").toUpperCase();

        try {
            if (studentDAO.getStudentByRollNo(rollNo) != null) {
                ConsoleUtils.printError("Student with Roll Number '" + rollNo + "' already exists!");
                ConsoleUtils.pressEnterToContinue();
                return;
            }

            String name = ConsoleUtils.readNonEmptyString("Enter Full Name: ");
            String email = ConsoleUtils.readString("Enter Email (optional): ");
            String phone = ConsoleUtils.readString("Enter Phone Number (optional): ");
            String course = ConsoleUtils.readNonEmptyString("Enter Course / Branch (e.g., B.Tech CSE, BCA): ");
            String semester = ConsoleUtils.readString("Enter Semester (e.g., 3rd Sem, 4): ");
            String batch = ConsoleUtils.readString("Enter Batch (e.g., 2024-2028): ");

            Student student = new Student(rollNo, name, email, phone, course, semester, batch);
            boolean created = studentDAO.addStudent(student);

            if (created) {
                ConsoleUtils.printSuccess("Student '" + name + "' added successfully with ID: " + student.getId());
            } else {
                ConsoleUtils.printError("Failed to add student.");
            }
        } catch (SQLException e) {
            ConsoleUtils.printError("Database Error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private static void viewAllStudents() {
        ConsoleUtils.printSubHeader("Registered Students");
        try {
            List<Student> students = studentDAO.getAllStudents();
            printStudentTable(students);
        } catch (SQLException e) {
            ConsoleUtils.printError("Database Error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private static void searchStudent() {
        ConsoleUtils.printSubHeader("Search Student");
        String query = ConsoleUtils.readNonEmptyString("Enter search keyword (Roll No, Name, or Course): ");
        try {
            List<Student> results = studentDAO.searchStudents(query);
            printStudentTable(results);
        } catch (SQLException e) {
            ConsoleUtils.printError("Database Error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private static void updateStudent() {
        ConsoleUtils.printSubHeader("Update Student Details");
        String rollNo = ConsoleUtils.readNonEmptyString("Enter Roll Number to update: ").toUpperCase();
        try {
            Student student = studentDAO.getStudentByRollNo(rollNo);
            if (student == null) {
                ConsoleUtils.printError("Student not found with Roll Number: " + rollNo);
                ConsoleUtils.pressEnterToContinue();
                return;
            }

            System.out.println(ConsoleUtils.BLUE + "Current Data: " + student + ConsoleUtils.RESET);
            System.out.println("(Press Enter to keep current value)");

            String name = ConsoleUtils.readString("New Name [" + student.getName() + "]: ");
            if (!name.isEmpty()) student.setName(name);

            String email = ConsoleUtils.readString("New Email [" + student.getEmail() + "]: ");
            if (!email.isEmpty()) student.setEmail(email);

            String phone = ConsoleUtils.readString("New Phone [" + student.getPhone() + "]: ");
            if (!phone.isEmpty()) student.setPhone(phone);

            String course = ConsoleUtils.readString("New Course [" + student.getCourse() + "]: ");
            if (!course.isEmpty()) student.setCourse(course);

            String semester = ConsoleUtils.readString("New Semester [" + student.getSemester() + "]: ");
            if (!semester.isEmpty()) student.setSemester(semester);

            String batch = ConsoleUtils.readString("New Batch [" + student.getBatch() + "]: ");
            if (!batch.isEmpty()) student.setBatch(batch);

            boolean updated = studentDAO.updateStudent(student);
            if (updated) {
                ConsoleUtils.printSuccess("Student record updated successfully!");
            } else {
                ConsoleUtils.printError("Failed to update student.");
            }
        } catch (SQLException e) {
            ConsoleUtils.printError("Database Error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private static void deleteStudent() {
        ConsoleUtils.printSubHeader("Delete Student");
        String rollNo = ConsoleUtils.readNonEmptyString("Enter Roll Number to delete: ").toUpperCase();
        try {
            Student student = studentDAO.getStudentByRollNo(rollNo);
            if (student == null) {
                ConsoleUtils.printError("Student not found with Roll Number: " + rollNo);
                ConsoleUtils.pressEnterToContinue();
                return;
            }

            ConsoleUtils.printWarning("Are you sure you want to delete '" + student.getName() + "' (" + rollNo + ")?");
            String confirm = ConsoleUtils.readString("Type 'YES' to confirm: ");
            if ("YES".equalsIgnoreCase(confirm.trim())) {
                boolean deleted = studentDAO.deleteStudentByRollNo(rollNo);
                if (deleted) {
                    ConsoleUtils.printSuccess("Student and associated attendance records deleted.");
                } else {
                    ConsoleUtils.printError("Could not delete student.");
                }
            } else {
                ConsoleUtils.printInfo("Deletion cancelled.");
            }
        } catch (SQLException e) {
            ConsoleUtils.printError("Database Error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private static void printStudentTable(List<Student> students) {
        if (students.isEmpty()) {
            ConsoleUtils.printWarning("No student records found.");
            return;
        }

        System.out.println("\n+-----+---------------+----------------------+-----------------+---------------+----------+");
        System.out.printf("| %-3s | %-13s | %-20s | %-15s | %-13s | %-8s |\n",
                "ID", "Roll No", "Name", "Course", "Semester", "Batch");
        System.out.println("+-----+---------------+----------------------+-----------------+---------------+----------+");

        for (Student s : students) {
            System.out.printf("| %-3d | %-13s | %-20s | %-15s | %-13s | %-8s |\n",
                    s.getId(),
                    truncate(s.getRollNo(), 13),
                    truncate(s.getName(), 20),
                    truncate(s.getCourse(), 15),
                    truncate(s.getSemester(), 13),
                    truncate(s.getBatch(), 8));
        }
        System.out.println("+-----+---------------+----------------------+-----------------+---------------+----------+");
        System.out.println("Total: " + students.size() + " student(s)\n");
    }

    // ==========================================
    //  ATTENDANCE MANAGEMENT
    // ==========================================

    private static void attendanceMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("ATTENDANCE MANAGEMENT");
            System.out.println("  1. Mark Batch Attendance (Full Class by Subject & Date)");
            System.out.println("  2. Mark / Update Single Student Attendance");
            System.out.println("  3. View Attendance Sheet (by Date & Subject)");
            System.out.println("  4. View Student Attendance History");
            System.out.println("  5. Back to Main Menu");
            System.out.println("───────────────────────────────────────────────────────────");

            int choice = ConsoleUtils.readIntInRange("Select an option [1-5]: ", 1, 5);
            switch (choice) {
                case 1 -> markBatchAttendance();
                case 2 -> markSingleAttendance();
                case 3 -> viewAttendanceSheet();
                case 4 -> viewStudentAttendanceHistory();
                case 5 -> back = true;
            }
        }
    }

    private static void markBatchAttendance() {
        ConsoleUtils.printSubHeader("Mark Class Attendance (Batch Mode)");
        try {
            List<Student> students = studentDAO.getAllStudents();
            if (students.isEmpty()) {
                ConsoleUtils.printError("No registered students found! Please add students first.");
                ConsoleUtils.pressEnterToContinue();
                return;
            }

            displayAvailableSubjects();
            String subjectCode = ConsoleUtils.readNonEmptyString("Enter Subject Code: ").toUpperCase();
            LocalDate date = ConsoleUtils.readDate("Enter Attendance Date", true);

            System.out.println("\nMarking attendance for " + students.size() + " students on " + date + " for " + subjectCode + ":");
            System.out.println("(Shortcuts: P = Present, A = Absent, L = Late)\n");

            int markedCount = 0;
            for (Student s : students) {
                String prompt = String.format("[%s] %s ->", s.getRollNo(), s.getName());
                String status = ConsoleUtils.readAttendanceStatus(prompt);
                attendanceDAO.markAttendance(s.getId(), subjectCode, date, status, "");
                markedCount++;
            }

            ConsoleUtils.printSuccess("Attendance recorded successfully for " + markedCount + " students!");
        } catch (SQLException e) {
            ConsoleUtils.printError("Database Error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private static void markSingleAttendance() {
        ConsoleUtils.printSubHeader("Mark Single Student Attendance");
        String rollNo = ConsoleUtils.readNonEmptyString("Enter Student Roll No: ").toUpperCase();
        try {
            Student student = studentDAO.getStudentByRollNo(rollNo);
            if (student == null) {
                ConsoleUtils.printError("Student not found with Roll Number: " + rollNo);
                ConsoleUtils.pressEnterToContinue();
                return;
            }

            displayAvailableSubjects();
            String subjectCode = ConsoleUtils.readNonEmptyString("Enter Subject Code: ").toUpperCase();
            LocalDate date = ConsoleUtils.readDate("Enter Attendance Date", true);
            String status = ConsoleUtils.readAttendanceStatus("Status for " + student.getName() + ":");
            String remarks = ConsoleUtils.readString("Remarks (optional): ");

            boolean success = attendanceDAO.markAttendance(student.getId(), subjectCode, date, status, remarks);
            if (success) {
                ConsoleUtils.printSuccess("Attendance recorded: " + student.getName() + " -> " + status + " on " + date);
            } else {
                ConsoleUtils.printError("Failed to record attendance.");
            }
        } catch (SQLException e) {
            ConsoleUtils.printError("Database Error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private static void viewAttendanceSheet() {
        ConsoleUtils.printSubHeader("View Attendance Sheet");
        try {
            displayAvailableSubjects();
            String subjectCode = ConsoleUtils.readNonEmptyString("Enter Subject Code: ").toUpperCase();
            LocalDate date = ConsoleUtils.readDate("Enter Date", true);

            List<AttendanceRecord> records = attendanceDAO.getAttendanceByDateAndSubject(date, subjectCode);
            if (records.isEmpty()) {
                ConsoleUtils.printWarning("No attendance records found for " + subjectCode + " on " + date + ".");
            } else {
                System.out.println("\n+-----+---------------+----------------------+---------------+-----------+---------------------+");
                System.out.printf("| %-3s | %-13s | %-20s | %-13s | %-9s | %-19s |\n",
                        "#", "Roll No", "Student Name", "Subject", "Status", "Remarks");
                System.out.println("+-----+---------------+----------------------+---------------+-----------+---------------------+");

                int idx = 1;
                for (AttendanceRecord r : records) {
                    String statusCol = switch (r.getStatus().toUpperCase()) {
                        case "PRESENT" -> ConsoleUtils.GREEN + "PRESENT" + ConsoleUtils.RESET;
                        case "ABSENT" -> ConsoleUtils.RED + "ABSENT " + ConsoleUtils.RESET;
                        default -> ConsoleUtils.YELLOW + "LATE   " + ConsoleUtils.RESET;
                    };

                    System.out.printf("| %-3d | %-13s | %-20s | %-13s | %s | %-19s |\n",
                            idx++,
                            truncate(r.getStudentRollNo(), 13),
                            truncate(r.getStudentName(), 20),
                            truncate(r.getSubjectCode(), 13),
                            statusCol,
                            truncate(r.getRemarks(), 19));
                }
                System.out.println("+-----+---------------+----------------------+---------------+-----------+---------------------+");
            }
        } catch (SQLException e) {
            ConsoleUtils.printError("Database Error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private static void viewStudentAttendanceHistory() {
        ConsoleUtils.printSubHeader("Student Attendance History");
        String rollNo = ConsoleUtils.readNonEmptyString("Enter Roll Number: ").toUpperCase();
        try {
            Student student = studentDAO.getStudentByRollNo(rollNo);
            if (student == null) {
                ConsoleUtils.printError("Student not found with Roll Number: " + rollNo);
                ConsoleUtils.pressEnterToContinue();
                return;
            }

            List<AttendanceRecord> records = attendanceDAO.getAttendanceByStudent(student.getId());
            if (records.isEmpty()) {
                ConsoleUtils.printWarning("No attendance logs found for " + student.getName() + " (" + rollNo + ").");
            } else {
                System.out.println("\nAttendance Logs for: " + ConsoleUtils.BOLD + student.getName() + " (" + student.getRollNo() + ")" + ConsoleUtils.RESET);
                System.out.println("+-----+------------+---------------+-----------+-------------------------------+");
                System.out.printf("| %-3s | %-10s | %-13s | %-9s | %-29s |\n", "#", "Date", "Subject", "Status", "Remarks");
                System.out.println("+-----+------------+---------------+-----------+-------------------------------+");

                int idx = 1;
                for (AttendanceRecord r : records) {
                    String statusCol = switch (r.getStatus().toUpperCase()) {
                        case "PRESENT" -> ConsoleUtils.GREEN + "PRESENT" + ConsoleUtils.RESET;
                        case "ABSENT" -> ConsoleUtils.RED + "ABSENT " + ConsoleUtils.RESET;
                        default -> ConsoleUtils.YELLOW + "LATE   " + ConsoleUtils.RESET;
                    };

                    System.out.printf("| %-3d | %-10s | %-13s | %s | %-29s |\n",
                            idx++,
                            r.getAttendanceDate(),
                            truncate(r.getSubjectCode(), 13),
                            statusCol,
                            truncate(r.getRemarks(), 29));
                }
                System.out.println("+-----+------------+---------------+-----------+-------------------------------+");
            }
        } catch (SQLException e) {
            ConsoleUtils.printError("Database Error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    // ==========================================
    //  REPORTS & DEFAULTERS
    // ==========================================

    private static void reportsMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("REPORTS & ANALYTICS");
            System.out.println("  1. Overall Attendance Summary (All Students)");
            System.out.println("  2. Subject-wise Attendance Report");
            System.out.println("  3. Attendance Defaulters List (< 75% or custom threshold)");
            System.out.println("  4. Back to Main Menu");
            System.out.println("───────────────────────────────────────────────────────────");

            int choice = ConsoleUtils.readIntInRange("Select an option [1-4]: ", 1, 4);
            switch (choice) {
                case 1 -> generateReport("ALL");
                case 2 -> {
                    displayAvailableSubjects();
                    String code = ConsoleUtils.readNonEmptyString("Enter Subject Code: ").toUpperCase();
                    generateReport(code);
                }
                case 3 -> generateDefaultersReport();
                case 4 -> back = true;
            }
        }
    }

    private static void generateReport(String subjectFilter) {
        ConsoleUtils.printSubHeader("Attendance Summary Report " + (subjectFilter.equals("ALL") ? "(Overall)" : "(Subject: " + subjectFilter + ")"));
        try {
            List<AttendanceReport> reports = attendanceDAO.getAttendanceReport(subjectFilter);
            printReportTable(reports, 75.0);
        } catch (SQLException e) {
            ConsoleUtils.printError("Database Error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private static void generateDefaultersReport() {
        ConsoleUtils.printSubHeader("Defaulters Alert Report (Low Attendance)");
        double rawThreshold = ConsoleUtils.readDouble("Enter minimum required attendance percentage (Default: 75.0): ");
        final double finalThreshold = (rawThreshold <= 0 || rawThreshold > 100) ? 75.0 : rawThreshold;

        String filter = ConsoleUtils.readString("Enter Subject Code or press [Enter] for Overall: ").toUpperCase();
        if (filter.isEmpty()) filter = "ALL";

        try {
            List<AttendanceReport> reports = attendanceDAO.getAttendanceReport(filter);
            List<AttendanceReport> defaulters = reports.stream()
                    .filter(r -> r.getTotalClasses() > 0 && r.getPercentage() < finalThreshold)
                    .toList();

            if (defaulters.isEmpty()) {
                ConsoleUtils.printSuccess("Great news! No student is below " + String.format("%.1f", finalThreshold) + "% attendance threshold.");
            } else {
                ConsoleUtils.printWarning("Found " + defaulters.size() + " student(s) below " + String.format("%.1f", finalThreshold) + "% attendance threshold!");
                printReportTable(defaulters, finalThreshold);
            }
        } catch (SQLException e) {
            ConsoleUtils.printError("Database Error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private static void printReportTable(List<AttendanceReport> reports, double threshold) {
        if (reports.isEmpty()) {
            ConsoleUtils.printWarning("No attendance records found.");
            return;
        }

        System.out.println("\n+---------------+----------------------+---------+---------+---------+--------+------------+------------+");
        System.out.printf("| %-13s | %-20s | %-7s | %-7s | %-7s | %-6s | %-10s | %-10s |\n",
                "Roll No", "Student Name", "Total", "Present", "Absent", "Late", "Attd %", "Eligibility");
        System.out.println("+---------------+----------------------+---------+---------+---------+--------+------------+------------+");

        for (AttendanceReport r : reports) {
            double pct = r.getPercentage();
            String pctColor = (pct >= threshold) ? ConsoleUtils.GREEN : (pct >= 50.0 ? ConsoleUtils.YELLOW : ConsoleUtils.RED);
            String eligibility = (pct >= threshold) ? (ConsoleUtils.GREEN + "ELIGIBLE" + ConsoleUtils.RESET) : (ConsoleUtils.RED + "DEFAULTER" + ConsoleUtils.RESET);

            System.out.printf("| %-13s | %-20s | %-7d | %-7d | %-7d | %-6d | %s%7.2f%%%s | %-10s |\n",
                    truncate(r.getRollNo(), 13),
                    truncate(r.getName(), 20),
                    r.getTotalClasses(),
                    r.getPresentCount(),
                    r.getAbsentCount(),
                    r.getLateCount(),
                    pctColor, pct, ConsoleUtils.RESET,
                    eligibility);
        }
        System.out.println("+---------------+----------------------+---------+---------+---------+--------+------------+------------+");
    }

    // ==========================================
    //  SUBJECT MANAGEMENT
    // ==========================================

    private static void subjectMenu() {
        boolean back = false;
        while (!back) {
            ConsoleUtils.printHeader("SUBJECT MANAGEMENT");
            System.out.println("  1. Add New Subject");
            System.out.println("  2. View All Subjects");
            System.out.println("  3. Back to Main Menu");
            System.out.println("───────────────────────────────────────────────────────────");

            int choice = ConsoleUtils.readIntInRange("Select an option [1-3]: ", 1, 3);
            switch (choice) {
                case 1 -> addNewSubject();
                case 2 -> viewAllSubjects();
                case 3 -> back = true;
            }
        }
    }

    private static void addNewSubject() {
        ConsoleUtils.printSubHeader("Add New Subject");
        String code = ConsoleUtils.readNonEmptyString("Enter Subject Code (e.g., CS105): ").toUpperCase();

        try {
            if (subjectDAO.subjectExists(code)) {
                ConsoleUtils.printError("Subject code '" + code + "' already exists!");
                ConsoleUtils.pressEnterToContinue();
                return;
            }

            String name = ConsoleUtils.readNonEmptyString("Enter Subject Name: ");
            String instructor = ConsoleUtils.readString("Enter Instructor Name (optional): ");

            Subject subject = new Subject(code, name, instructor);
            boolean created = subjectDAO.addSubject(subject);

            if (created) {
                ConsoleUtils.printSuccess("Subject '" + name + " (" + code + ")' added successfully!");
            } else {
                ConsoleUtils.printError("Failed to add subject.");
            }
        } catch (SQLException e) {
            ConsoleUtils.printError("Database Error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private static void viewAllSubjects() {
        ConsoleUtils.printSubHeader("Course Subjects");
        try {
            List<Subject> list = subjectDAO.getAllSubjects();
            if (list.isEmpty()) {
                ConsoleUtils.printWarning("No subjects found.");
            } else {
                System.out.println("\n+-----+---------------+------------------------------------+----------------------+");
                System.out.printf("| %-3s | %-13s | %-34s | %-20s |\n", "#", "Code", "Subject Name", "Instructor");
                System.out.println("+-----+---------------+------------------------------------+----------------------+");
                int idx = 1;
                for (Subject s : list) {
                    System.out.printf("| %-3d | %-13s | %-34s | %-20s |\n",
                            idx++,
                            truncate(s.getCode(), 13),
                            truncate(s.getName(), 34),
                            truncate(s.getInstructor(), 20));
                }
                System.out.println("+-----+---------------+------------------------------------+----------------------+");
            }
        } catch (SQLException e) {
            ConsoleUtils.printError("Database Error: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private static void displayAvailableSubjects() {
        try {
            List<Subject> subjects = subjectDAO.getAllSubjects();
            if (!subjects.isEmpty()) {
                System.out.print(ConsoleUtils.CYAN + "Available Subjects: " + ConsoleUtils.RESET);
                for (int i = 0; i < subjects.size(); i++) {
                    System.out.print(subjects.get(i).getCode() + " (" + subjects.get(i).getName() + ")" + (i < subjects.size() - 1 ? ", " : ""));
                }
                System.out.println();
            }
        } catch (SQLException ignored) {
        }
    }

    private static void showDatabaseInfo() {
        ConsoleUtils.printHeader("DATABASE CONFIGURATION");
        System.out.println("  DB Engine  : " + DBConnection.getDbType().toUpperCase());
        System.out.println("  JDBC URL   : " + DBConnection.getDbUrl());
        System.out.println("  Driver     : " + ("mysql".equalsIgnoreCase(DBConnection.getDbType()) ? "com.mysql.cj.jdbc.Driver" : "org.sqlite.JDBC"));

        try {
            int count = studentDAO.getStudentCount();
            ConsoleUtils.printSuccess("Connection test passed! Total students in database: " + count);
        } catch (SQLException e) {
            ConsoleUtils.printError("Connection test failed: " + e.getMessage());
        }
        ConsoleUtils.pressEnterToContinue();
    }

    private static String truncate(String text, int max) {
        if (text == null) return "-";
        if (text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
    }
}
