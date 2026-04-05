package dao;

import models.Registration;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDAO {

    /**
     * Registers multiple courses for a student.
     * Uses INSERT IGNORE so already-registered courses are silently skipped.
     * Returns the number of courses actually inserted (0 if nothing new).
     * Returns -1 if a database/schema error occurred.
     */
    public int registerMultipleCourses(String studentId, String studentName, List<String> courseIds) {
        if (studentId == null || studentId.isBlank() || courseIds == null || courseIds.isEmpty()) {
            return -1;
        }
        String safeStudentName = (studentName == null || studentName.isBlank()) ? studentId : studentName;

        // INSERT IGNORE: silently skips rows that violate the UNIQUE(student_id, course_id) key
        String query = "INSERT INTO registrations (student_id, student_name, course_id, registration_date)"
                     + " VALUES (?, ?, ?, CURDATE()) ON DUPLICATE KEY UPDATE student_id=student_id";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("registerMultipleCourses: no DB connection.");
            return -1;
        }
        int inserted = 0;
        try {
            // Insert each course individually so one bad course doesn't abort the rest
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                for (String courseId : courseIds) {
                    if (courseId == null || courseId.isBlank()) continue;
                    stmt.setString(1, studentId);
                    stmt.setString(2, safeStudentName);
                    stmt.setString(3, courseId);
                    inserted += stmt.executeUpdate(); // 1 if inserted, 0 if ignored (duplicate)
                }
            }
            return inserted;
        } catch (SQLException e) {
            System.err.println("registerMultipleCourses SQL error: " + e.getMessage());
            e.printStackTrace();
            return -1; // -1 signals a real DB/schema error (not a duplicate)
        } finally {
            DatabaseConnection.disconnect(conn);
        }
    }

    public List<Registration> getStudentRegistrations(String studentId) {
        List<Registration> registrations = new ArrayList<>();
        if (studentId == null || studentId.isBlank()) return registrations;

        // Use LEFT JOIN so the UI can show registered course IDs even if the course record is missing.
        // Allow looking up by either student_id or student_name to support cases where login username differs from student_id.
        String query = "SELECT r.id, r.student_id, r.student_name, r.course_id, r.registration_date, "
                     + "COALESCE(c.course_name, r.course_id) AS course_name, "
                     + "c.credits, c.semester "
                     + "FROM registrations r LEFT JOIN courses c ON r.course_id = c.course_id "
                     + "WHERE r.student_id = ? OR r.student_name = ? "
                     + "ORDER BY c.semester, r.course_id";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return registrations;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, studentId);
            stmt.setString(2, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Registration reg = new Registration(
                        rs.getInt("id"),
                        rs.getString("student_id"),
                        rs.getString("student_name"),
                        rs.getString("course_id"),
                        rs.getDate("registration_date")
                    );
                    reg.setCourseName(rs.getString("course_name"));
                    reg.setCredits(rs.getInt("credits"));
                    reg.setSemester(rs.getInt("semester"));
                    registrations.add(reg);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return registrations;
    }

    /**
     * Returns a summary list: each row = [student_id, student_name, courseCount]
     * Used by ViewStudentPanel to show one row per student.
     */
    public List<Object[]> getStudentSummary() {
        List<Object[]> summary = new ArrayList<>();
        String query = "SELECT student_id, student_name, COUNT(*) AS course_count "
                     + "FROM registrations GROUP BY student_id, student_name ORDER BY student_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                summary.add(new Object[]{
                    rs.getString("student_id"),
                    rs.getString("student_name"),
                    rs.getInt("course_count")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary;
    }

    public boolean deleteRegistration(String studentId, String courseId) {
        if (studentId == null || courseId == null) return false;
        String query = "DELETE FROM registrations WHERE student_id = ? AND course_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.disconnect(conn);
        }
    }

    public boolean deleteAllRegistrations(String studentId) {
        if (studentId == null) return false;
        String query = "DELETE FROM registrations WHERE student_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, studentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.disconnect(conn);
        }
    }

    public boolean updateRegistrationCourse(String studentId, String oldCourseId, String newCourseId) {
        if (studentId == null || oldCourseId == null || newCourseId == null) return false;
        String query = "UPDATE registrations SET course_id = ? WHERE student_id = ? AND course_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newCourseId);
            stmt.setString(2, studentId);
            stmt.setString(3, oldCourseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStudentId(String oldStudentId, String newStudentId) {
        if (oldStudentId == null || newStudentId == null || oldStudentId.isBlank() || newStudentId.isBlank()) return false;
        String query = "UPDATE registrations SET student_id = ? WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newStudentId);
            stmt.setString(2, oldStudentId);
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStudentName(String studentId, String newStudentName) {
        if (studentId == null || newStudentName == null || newStudentName.isBlank()) return false;
        String query = "UPDATE registrations SET student_name = ? WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newStudentName);
            stmt.setString(2, studentId);
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getRegistrationCount(String studentId) {
        if (studentId == null) return 0;
        String query = "SELECT COUNT(*) FROM registrations WHERE student_id = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) return 0;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return 0;
    }

    /**
     * Finds the student_id (roll number) associated with a student_name (username).
     */
    public String getStudentIdByUsername(String username) {
        if (username == null || username.isBlank()) return null;
        String query = "SELECT student_id FROM registrations WHERE student_name = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("student_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}