#!/bin/bash
set -e

# Detect Java runtime
if [ -f "/Users/maheshdubey/Documents/jdk-26.0.1.jdk/Contents/Home/bin/java" ]; then
    JAVA="/Users/maheshdubey/Documents/jdk-26.0.1.jdk/Contents/Home/bin/java"
elif command -v java >/dev/null 2>&1; then
    JAVA="java"
else
    echo "Error: java runtime not found."
    exit 1
fi

# Compile if bin directory does not exist or files are modified
if [ ! -d "bin" ] || [ ! -f "bin/com/attendance/web/AttendanceWebServer.class" ]; then
    ./compile.sh
fi

MODE="${1:-web}"

if [ "$MODE" = "cli" ]; then
    echo "Starting Attendance Management CLI System..."
    $JAVA -cp "bin:lib/*" com.attendance.AttendanceApp
else
    PORT="${2:-8080}"
    echo "Starting Attendance Management Web Server on port $PORT..."
    $JAVA -cp "bin:lib/*" com.attendance.web.AttendanceWebServer "$PORT"
fi
