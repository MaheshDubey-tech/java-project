package com.attendance.model;

/**
 * Model representing aggregated attendance metrics for a student.
 */
public class AttendanceReport {
    private int studentId;
    private String rollNo;
    private String name;
    private String course;
    private String subjectCode;
    private int totalClasses;
    private int presentCount;
    private int absentCount;
    private int lateCount;

    public AttendanceReport() {
    }

    public AttendanceReport(int studentId, String rollNo, String name, String course, String subjectCode,
                            int totalClasses, int presentCount, int absentCount, int lateCount) {
        this.studentId = studentId;
        this.rollNo = rollNo;
        this.name = name;
        this.course = course;
        this.subjectCode = subjectCode;
        this.totalClasses = totalClasses;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.lateCount = lateCount;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(int presentCount) {
        this.presentCount = presentCount;
    }

    public int getAbsentCount() {
        return absentCount;
    }

    public void setAbsentCount(int absentCount) {
        this.absentCount = absentCount;
    }

    public int getLateCount() {
        return lateCount;
    }

    public void setLateCount(int lateCount) {
        this.lateCount = lateCount;
    }

    /**
     * Calculates the attendance percentage.
     * Present counts as 1.0, Late can count as 0.5 or 1.0 (let's consider Late as present with note or full, here Present + Late).
     * Standard: (Present + Late) / Total * 100.
     */
    public double getPercentage() {
        if (totalClasses == 0) return 0.0;
        return ((double) (presentCount + lateCount) / totalClasses) * 100.0;
    }

    public boolean isDefaulter(double threshold) {
        return getPercentage() < threshold;
    }
}
