package dao;

import models.Course;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    // Retrieve courses by semester
    public List<Course> getCoursesBySemester(int semester) {
        List<Course> courses = new ArrayList<>();
        String query = "SELECT * FROM courses WHERE semester = ?";
        
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Cannot load courses: no database connection.");
            return courses;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, semester);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                courses.add(new Course(
                    rs.getString("course_id"),
                    rs.getString("course_name"),
                    rs.getInt("semester"),
                    rs.getInt("credits")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String query = "SELECT * FROM courses ORDER BY semester, course_id";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Cannot load courses: no database connection.");
            return courses;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                courses.add(new Course(
                    rs.getString("course_id"),
                    rs.getString("course_name"),
                    rs.getInt("semester"),
                    rs.getInt("credits")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return courses;
    }
}
