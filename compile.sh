#!/bin/bash
set -e

# Detect Java compiler
if [ -f "/Users/maheshdubey/Documents/jdk-26.0.1.jdk/Contents/Home/bin/javac" ]; then
    JAVAC="/Users/maheshdubey/Documents/jdk-26.0.1.jdk/Contents/Home/bin/javac"
elif command -v javac >/dev/null 2>&1; then
    JAVAC="javac"
else
    echo "Error: javac not found."
    exit 1
fi

echo "Compiling Java Attendance Management System using $JAVAC..."
mkdir -p bin
$JAVAC -d bin -cp "lib/*:src" src/com/attendance/model/*.java src/com/attendance/dao/*.java src/com/attendance/util/*.java src/com/attendance/*.java src/com/attendance/web/*.java
echo "✔ Compilation finished successfully!"
