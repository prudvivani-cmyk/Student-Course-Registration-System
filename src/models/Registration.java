package models;

import java.sql.Date;

public class Registration {
    private int id;
    private String studentId;
    private String studentName;
    private String courseId;
    private Date registrationDate;

    // Additional fields for convenience when joining with courses table
    private String courseName;
    private int credits;
    private int semester;

    public Registration() {}

    public Registration(int id, String studentId, String studentName, String courseId, Date registrationDate) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseId = courseId;
        this.registrationDate = registrationDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public Date getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
}
