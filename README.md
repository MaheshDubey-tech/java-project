# 🎓 Student Attendance Management System

A complete **Student Attendance Management System** built with **Java** and **JDBC** (`java.sql.PreparedStatement`). It runs as a local web application on `http://localhost:8080` (powered by Java SE's built-in `HttpServer`), and also includes a full-featured terminal CLI interface.

---

## ✨ Features

1. **Student Details Management**:
   - Register students with Roll Number, Name, Email, Phone, Course/Branch, Semester, and Academic Batch.
   - Live search, filtering, and table view.
   - Update and delete student profiles (with cascading attendance deletion).

2. **Course & Subject Catalog**:
   - Add curriculum subjects, course codes (e.g. `CS101`, `CS102`), and instructor details.

3. **Attendance Tracking & Logging**:
   - **Batch Attendance Mode**: Mark attendance for an entire class roster in seconds.
   - **Status Types**: `PRESENT` (P), `ABSENT` (A), `LATE` (L).
   - Instant toggle with auto-calculated summaries.
   - Safe against duplicates: Uses SQL `UPSERT` (`ON CONFLICT` / `ON DUPLICATE KEY UPDATE`).

4. **Analytics & Defaulters Tracking**:
   - Overall and subject-specific attendance percentage calculation.
   - Real-time **Defaulters Alert** for students with attendance below threshold (default `< 75%`).
   - Exam eligibility status badge (`ELIGIBLE` vs `DEFAULTER`).

5. **Database Architecture**:
   - **SQLite (Default)**: Embedded, zero-configuration local database (`attendance.db`).
   - **MySQL**: Supported out of the box by updating `db.properties`.
   - Automatic table creation and initial seed data on first run.

---

## 🚀 Running on Localhost

The server is currently running on:
👉 **[http://localhost:8080](http://localhost:8080)**

### Quick Run Commands:
```bash
# Start Web Server on localhost:8080
./run.sh web

# Or via npm
npm run dev

# Run Interactive Terminal CLI Mode
./run.sh cli
```

---

## 📁 Project Architecture

```
java project/
├── lib/
│   ├── sqlite-jdbc-3.45.1.0.jar      # SQLite JDBC Driver
│   ├── mysql-connector-j-8.3.0.jar   # MySQL JDBC Driver
│   ├── slf4j-api-2.0.12.jar          # Logging API
│   └── slf4j-simple-2.0.12.jar       # Simple Logger
├── src/
│   └── com/attendance/
│       ├── model/
│       │   ├── Student.java          # Student entity
│       │   ├── Subject.java          # Subject entity
│       │   ├── AttendanceRecord.java # Attendance log entity
│       │   └── AttendanceReport.java # Statistical metrics model
│       ├── dao/
│       │   ├── DBConnection.java     # JDBC Connection & Auto-DDL
│       │   ├── StudentDAO.java       # PreparedStatements for Students
│       │   ├── SubjectDAO.java       # PreparedStatements for Subjects
│       │   └── AttendanceDAO.java    # PreparedStatements for Attendance & Reports
│       ├── util/
│       │   └── ConsoleUtils.java     # Formatting & CLI helpers
│       ├── web/
│       │   └── AttendanceWebServer.java # Built-in JDK HttpServer (localhost:8080)
│       └── AttendanceApp.java        # Interactive Terminal CLI Main Class
├── web/
│   └── index.html                    # Modern Web Dashboard UI
├── db.properties                     # Database config (SQLite / MySQL)
├── schema.sql                        # SQL Schema definition
├── compile.sh                        # Compilation script
├── run.sh                            # Execution launcher
└── package.json                      # npm run dev / npm start scripts
```

---

## 🗄️ Database Configuration

By default, the application uses **SQLite** (`attendance.db`). To switch to **MySQL**:

1. Open [`db.properties`](file:///Users/maheshdubey/java%20project/db.properties):
```properties
db.type=mysql
mysql.url=jdbc:mysql://localhost:3306/attendance_db?createDatabaseIfNotExist=true&useSSL=false
mysql.user=root
mysql.password=your_password
```
2. Restart the application. Tables will be created automatically.
