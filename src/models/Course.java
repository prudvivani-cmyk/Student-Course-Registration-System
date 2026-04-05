package models;

public class Course {
    private String courseId;
    private String courseName;
    private int semester;
    private int credits;
    private double price;

    public Course() {}

    public Course(String courseId, String courseName, int semester, int credits) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.semester = semester;
        this.credits = credits;
        this.price = 500.0; // Default price
    }

    public Course(String courseId, String courseName, int semester, int credits, double price) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.semester = semester;
        this.credits = credits;
        this.price = price;
    }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
