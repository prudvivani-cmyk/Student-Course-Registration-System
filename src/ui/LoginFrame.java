package ui;

import dao.UserDAO;
import models.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private UserDAO userDAO;

    public LoginFrame() {
        userDAO = new UserDAO();
        setTitle("Student Course Registration System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        setContentPane(mainPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, gbc);

        // Username
        gbc.gridwidth = 1; gbc.gridy = 1;
        mainPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        mainPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        mainPanel.add(passwordField, gbc);

        // Role
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        roleComboBox = new JComboBox<>(new String[]{"student", "admin"});
        mainPanel.add(roleComboBox, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout());
        
        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> handleLogin());
        
        JButton signUpBtn = new JButton("Sign Up");
        signUpBtn.addActionListener(e -> handleRegister());
        
        JButton forgotBtn = new JButton("Forgot Password");
        forgotBtn.addActionListener(e -> handleForgotPassword());

        btnPanel.add(loginBtn);
        btnPanel.add(signUpBtn);
        btnPanel.add(forgotBtn);
        
        mainPanel.add(btnPanel, gbc);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        User user = userDAO.authenticateUser(username, password, role);
        if (user != null) {
            showSuccess("Login Successful! Welcome, " + username);
            new DashboardFrame(user);
            dispose();
        } else {
            if (userDAO.userExists(username)) {
                showError("Incorrect password. Please try again.");
            } else {
                showError("Username not found. Please Sign Up first.");
            }
        }
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password to sign up.");
            return;
        }
        if ("admin".equals(roleComboBox.getSelectedItem())) {
            showError("Admins cannot self-register.");
            return;
        }
        if (userDAO.userExists(username)) {
            showError("Username \"" + username + "\" already exists. Please choose another.");
        } else {
            if (userDAO.registerStudent(username, password)) {
                showSuccess("Account created for \"" + username + "\"! You can now log in.");
            } else {
                showError("Sign up failed. Please try again.");
            }
        }
    }

    private void handleForgotPassword() {
        JDialog dlg = new JDialog(this, "Reset Password", true);
        dlg.setSize(350, 250);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField targetUserField = new JTextField(15);
        panel.add(targetUserField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField newPwdField = new JPasswordField(15);
        panel.add(newPwdField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField confirmPwdField = new JPasswordField(15);
        panel.add(confirmPwdField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton saveBtn = new JButton("Reset Password");
        JButton cancelBtn = new JButton("Cancel");
        
        cancelBtn.addActionListener(e -> dlg.dispose());
        saveBtn.addActionListener(e -> {
            String uname = targetUserField.getText().trim();
            String newPwd = new String(newPwdField.getPassword()).trim();
            String confirm = new String(confirmPwdField.getPassword()).trim();
            
            if (uname.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
                showError("Please fill in all fields.");
                return;
            }
            if (!userDAO.userExists(uname)) {
                showError("Username \"" + uname + "\" does not exist.");
                return;
            }
            if (!newPwd.equals(confirm)) {
                showError("Passwords do not match.");
                return;
            }
            if (userDAO.updatePassword(uname, newPwd)) {
                showSuccess("Password reset successfully! You can now log in.");
                dlg.dispose();
            } else {
                showError("Failed to reset password. Try again.");
            }
        });
        
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        panel.add(btnPanel, gbc);

        dlg.setContentPane(panel);
        dlg.setVisible(true);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
