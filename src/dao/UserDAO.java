package dao;

import models.User;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // Authenticate a user
    public User authenticateUser(String username, String password, String role) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Cannot authenticate: no database connection.");
            return null;
        }
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return null;
    }

    // Check if user exists
    public boolean userExists(String username) {
        String query = "SELECT id FROM users WHERE username = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Cannot check userExists: no database connection.");
            return false;
        }
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return false;
    }

    // Register a new student
    public boolean registerStudent(String username, String password) {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, 'student')";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Cannot register student: no database connection.");
            return false;
        }
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return false;
    }

    // Reset password
    public boolean updatePassword(String username, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE username = ?";
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Cannot update password: no database connection.");
            return false;
        }
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, username);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return false;
    }

    // Update the username (student name)
    public boolean updateUsername(String oldUsername, String newUsername) {
        if (oldUsername == null || newUsername == null || newUsername.isBlank()) {
            return false;
        }
        String query = "UPDATE users SET username = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newUsername);
            stmt.setString(2, oldUsername);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
