package com.attendance.web;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.DBConnection;
import com.attendance.dao.StudentDAO;
import com.attendance.dao.SubjectDAO;
import com.attendance.model.AttendanceRecord;
import com.attendance.model.AttendanceReport;
import com.attendance.model.Student;
import com.attendance.model.Subject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Built-in Lightweight HTTP Server using standard Java SE libraries (jdk.httpserver).
 * Runs on localhost:8080 and connects directly to JDBC DAOs.
 */
public class AttendanceWebServer {
    private static final int DEFAULT_PORT = 8080;
    private static final StudentDAO studentDAO = new StudentDAO();
    private static final SubjectDAO subjectDAO = new SubjectDAO();
    private static final AttendanceDAO attendanceDAO = new AttendanceDAO();

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor() != null ? 
                    Executors.newVirtualThreadPerTaskExecutor() : Executors.newFixedThreadPool(10));

            // Static files handler
            server.createContext("/", new StaticFileHandler());

            // API handlers
            server.createContext("/api/stats", new StatsHandler());
            server.createContext("/api/students", new StudentsHandler());
            server.createContext("/api/subjects", new SubjectsHandler());
            server.createContext("/api/attendance", new AttendanceHandler());
            server.createContext("/api/reports", new ReportsHandler());

            server.start();

            System.out.println("============================================================");
            System.out.println("  🌐 ATTENDANCE MANAGEMENT SYSTEM - WEB SERVER RUNNING");
            System.out.println("  🚀 URL      : http://localhost:" + port);
            System.out.println("  🗄  DATABASE : " + DBConnection.getDbType().toUpperCase() + " (" + DBConnection.getDbUrl() + ")");
            System.out.println("============================================================");
            System.out.println("Open your browser and navigate to: http://localhost:" + port);

        } catch (IOException e) {
            System.err.println("Failed to start HTTP server on port " + port + ": " + e.getMessage());
        }
    }

    // Static Web File Handler
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            Path filePath = Paths.get("web" + path).normalize();
            if (!filePath.startsWith(Paths.get("web")) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
                filePath = Paths.get("web/index.html");
            }

            if (Files.exists(filePath)) {
                byte[] bytes = Files.readAllBytes(filePath);
                String contentType = getContentType(filePath.toString());
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String notFound = "<h1>404 Not Found</h1>";
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
            }
        }

        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css; charset=UTF-8";
            if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (path.endsWith(".json")) return "application/json; charset=UTF-8";
            if (path.endsWith(".svg")) return "image/svg+xml";
            if (path.endsWith(".png")) return "image/png";
            return "text/plain; charset=UTF-8";
        }
    }

    // API: System Overview & Stats
    static class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                int totalStudents = studentDAO.getStudentCount();
                int totalSubjects = subjectDAO.getAllSubjects().size();
                List<AttendanceReport> reports = attendanceDAO.getAttendanceReport("ALL");

                long totalPresent = 0;
                long totalClasses = 0;
                long defaulters = 0;

                for (AttendanceReport r : reports) {
                    totalPresent += (r.getPresentCount() + r.getLateCount());
                    totalClasses += r.getTotalClasses();
                    if (r.getTotalClasses() > 0 && r.getPercentage() < 75.0) {
                        defaulters++;
                    }
                }

                double avgRate = totalClasses > 0 ? ((double) totalPresent / totalClasses) * 100.0 : 0.0;

                String json = String.format("""
                    {
                        "totalStudents": %d,
                        "totalSubjects": %d,
                        "totalClassesConducted": %d,
                        "averageAttendance": %.1f,
                        "defaultersCount": %d,
                        "dbType": "%s"
                    }
                """, totalStudents, totalSubjects, totalClasses, avgRate, defaulters, DBConnection.getDbType());

                sendJson(exchange, 200, json);
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }

    // API: Students CRUD
    static class StudentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                switch (method) {
                    case "GET" -> {
                        List<Student> students = studentDAO.getAllStudents();
                        StringBuilder sb = new StringBuilder("[");
                        for (int i = 0; i < students.size(); i++) {
                            Student s = students.get(i);
                            sb.append(String.format("""
                                {
                                    "id": %d,
                                    "rollNo": "%s",
                                    "name": "%s",
                                    "email": "%s",
                                    "phone": "%s",
                                    "course": "%s",
                                    "semester": "%s",
                                    "batch": "%s"
                                }
                            """, s.getId(), escapeJson(s.getRollNo()), escapeJson(s.getName()),
                                escapeJson(s.getEmail()), escapeJson(s.getPhone()),
                                escapeJson(s.getCourse()), escapeJson(s.getSemester()), escapeJson(s.getBatch())));
                            if (i < students.size() - 1) sb.append(",");
                        }
                        sb.append("]");
                        sendJson(exchange, 200, sb.toString());
                    }
                    case "POST" -> {
                        String body = readBody(exchange);
                        Map<String, String> data = parseFormDataOrJson(body);

                        String rollNo = data.getOrDefault("rollNo", "").toUpperCase().trim();
                        String name = data.getOrDefault("name", "").trim();
                        String email = data.getOrDefault("email", "").trim();
                        String phone = data.getOrDefault("phone", "").trim();
                        String course = data.getOrDefault("course", "").trim();
                        String semester = data.getOrDefault("semester", "").trim();
                        String batch = data.getOrDefault("batch", "").trim();

                        if (rollNo.isEmpty() || name.isEmpty()) {
                            sendError(exchange, 400, "Roll Number and Name are required.");
                            return;
                        }

                        if (studentDAO.getStudentByRollNo(rollNo) != null) {
                            sendError(exchange, 409, "Student with Roll No '" + rollNo + "' already exists.");
                            return;
                        }

                        Student s = new Student(rollNo, name, email, phone, course, semester, batch);
                        boolean ok = studentDAO.addStudent(s);
                        if (ok) {
                            sendJson(exchange, 201, "{\"success\":true,\"message\":\"Student added successfully.\"}");
                        } else {
                            sendError(exchange, 500, "Could not add student.");
                        }
                    }
                    case "DELETE" -> {
                        Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());
                        String rollNo = q.get("rollNo");
                        if (rollNo == null || rollNo.isBlank()) {
                            sendError(exchange, 400, "rollNo parameter is required.");
                            return;
                        }
                        boolean ok = studentDAO.deleteStudentByRollNo(rollNo);
                        if (ok) {
                            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Student deleted successfully.\"}");
                        } else {
                            sendError(exchange, 404, "Student not found.");
                        }
                    }
                    default -> sendError(exchange, 405, "Method Not Allowed");
                }
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }

    // API: Subjects
    static class SubjectsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                if ("GET".equals(method)) {
                    List<Subject> subjects = subjectDAO.getAllSubjects();
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < subjects.size(); i++) {
                        Subject s = subjects.get(i);
                        sb.append(String.format("""
                            {"id":%d,"code":"%s","name":"%s","instructor":"%s"}
                        """, s.getId(), escapeJson(s.getCode()), escapeJson(s.getName()), escapeJson(s.getInstructor())));
                        if (i < subjects.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                    sendJson(exchange, 200, sb.toString());
                } else if ("POST".equals(method)) {
                    String body = readBody(exchange);
                    Map<String, String> data = parseFormDataOrJson(body);
                    String code = data.getOrDefault("code", "").toUpperCase().trim();
                    String name = data.getOrDefault("name", "").trim();
                    String instructor = data.getOrDefault("instructor", "").trim();

                    if (code.isEmpty() || name.isEmpty()) {
                        sendError(exchange, 400, "Subject code and name are required.");
                        return;
                    }

                    if (subjectDAO.subjectExists(code)) {
                        sendError(exchange, 409, "Subject code already exists.");
                        return;
                    }

                    boolean ok = subjectDAO.addSubject(new Subject(code, name, instructor));
                    if (ok) {
                        sendJson(exchange, 201, "{\"success\":true,\"message\":\"Subject added.\"}");
                    } else {
                        sendError(exchange, 500, "Could not add subject.");
                    }
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }

    // API: Attendance
    static class AttendanceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                if ("GET".equals(method)) {
                    Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());
                    String dateStr = q.getOrDefault("date", LocalDate.now().toString());
                    String subject = q.getOrDefault("subject", "CS101").toUpperCase();

                    LocalDate date = LocalDate.parse(dateStr);
                    List<AttendanceRecord> records = attendanceDAO.getAttendanceByDateAndSubject(date, subject);

                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < records.size(); i++) {
                        AttendanceRecord r = records.get(i);
                        sb.append(String.format("""
                            {
                                "id": %d,
                                "studentId": %d,
                                "rollNo": "%s",
                                "name": "%s",
                                "subjectCode": "%s",
                                "date": "%s",
                                "status": "%s",
                                "remarks": "%s"
                            }
                        """, r.getId(), r.getStudentId(), escapeJson(r.getStudentRollNo()), escapeJson(r.getStudentName()),
                             escapeJson(r.getSubjectCode()), r.getAttendanceDate(), r.getStatus(), escapeJson(r.getRemarks())));
                        if (i < records.size() - 1) sb.append(",");
                    }
                    sb.append("]");
                    sendJson(exchange, 200, sb.toString());

                } else if ("POST".equals(method)) {
                    String body = readBody(exchange);
                    Map<String, String> data = parseFormDataOrJson(body);

                    String subject = data.getOrDefault("subjectCode", "").toUpperCase().trim();
                    String dateStr = data.getOrDefault("date", LocalDate.now().toString());
                    LocalDate date = LocalDate.parse(dateStr);

                    // Check if batch or single
                    if (data.containsKey("entries")) {
                        // entries format: "studentId:STATUS;studentId:STATUS"
                        String[] pairs = data.get("entries").split(";");
                        int count = 0;
                        for (String p : pairs) {
                            if (p.trim().isEmpty()) continue;
                            String[] parts = p.split(":");
                            if (parts.length >= 2) {
                                int sId = Integer.parseInt(parts[0].trim());
                                String st = parts[1].trim();
                                attendanceDAO.markAttendance(sId, subject, date, st, "");
                                count++;
                            }
                        }
                        sendJson(exchange, 200, "{\"success\":true,\"marked\":" + count + "}");
                    } else {
                        // Single
                        int studentId = Integer.parseInt(data.get("studentId"));
                        String status = data.getOrDefault("status", "PRESENT");
                        String remarks = data.getOrDefault("remarks", "");
                        boolean ok = attendanceDAO.markAttendance(studentId, subject, date, status, remarks);
                        sendJson(exchange, 200, "{\"success\":" + ok + "}");
                    }
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }

    // API: Reports
    static class ReportsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());
                String subject = q.getOrDefault("subject", "ALL");

                List<AttendanceReport> reports = attendanceDAO.getAttendanceReport(subject);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < reports.size(); i++) {
                    AttendanceReport r = reports.get(i);
                    sb.append(String.format("""
                        {
                            "studentId": %d,
                            "rollNo": "%s",
                            "name": "%s",
                            "course": "%s",
                            "subjectCode": "%s",
                            "totalClasses": %d,
                            "presentCount": %d,
                            "absentCount": %d,
                            "lateCount": %d,
                            "percentage": %.2f,
                            "isDefaulter": %b
                        }
                    """, r.getStudentId(), escapeJson(r.getRollNo()), escapeJson(r.getName()),
                         escapeJson(r.getCourse()), escapeJson(r.getSubjectCode()),
                         r.getTotalClasses(), r.getPresentCount(), r.getAbsentCount(), r.getLateCount(),
                         r.getPercentage(), r.isDefaulter(75.0)));
                    if (i < reports.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } catch (Exception e) {
                sendError(exchange, 500, e.getMessage());
            }
        }
    }

    // Utility methods
    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendError(HttpExchange exchange, int status, String message) throws IOException {
        String json = "{\"error\": \"" + escapeJson(message) + "\"}";
        sendJson(exchange, status, json);
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return out.toString(StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;
        String[] pairs = query.split("&");
        for (String p : pairs) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            } else if (kv.length == 1) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), "");
            }
        }
        return map;
    }

    private static Map<String, String> parseFormDataOrJson(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isBlank()) return map;
        body = body.trim();

        if (body.startsWith("{") && body.endsWith("}")) {
            // Basic json parser for simple key-value pairs
            String inner = body.substring(1, body.length() - 1);
            String[] tokens = inner.split(",");
            for (String t : tokens) {
                String[] kv = t.split(":", 2);
                if (kv.length == 2) {
                    String key = kv[0].replaceAll("[\"{}\\s]", "");
                    String val = kv[1].replaceAll("[\"{}\\s]", "");
                    map.put(key, val);
                }
            }
        } else {
            // URL Encoded form
            String[] pairs = body.split("&");
            for (String p : pairs) {
                String[] kv = p.split("=", 2);
                if (kv.length == 2) {
                    map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                }
            }
        }
        return map;
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
