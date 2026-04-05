package ui;

import dao.CourseDAO;
import dao.RegistrationDAO;
import dao.UserDAO;
import models.Course;
import models.Registration;
import models.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UpdateRegistrationPanel extends JPanel {

    private User currentUser;
    private JTextField searchStudentIdField;
    private JTextField newStudentNameField;
    private JPasswordField newPasswordField;
    private JComboBox<String> existingCourseCombo;
    private JComboBox<String> newCourseCombo;
    private JButton updateCourseBtn;
    private String currentlyLoadedName = "";

    private UserDAO userDAO;
    private RegistrationDAO registrationDAO;
    private CourseDAO courseDAO;

    public UpdateRegistrationPanel(User currentUser) {
        this.currentUser = currentUser;
        this.userDAO = new UserDAO();
        this.registrationDAO = new RegistrationDAO();
        this.courseDAO = new CourseDAO();

        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);

        // -- Update Info Section
        gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 3;
        formPanel.add(new JLabel("Update Student Details (leave blank to skip)"), gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 1; gbc.gridx = 0;
        formPanel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1;
        searchStudentIdField = new JTextField(15);
        formPanel.add(searchStudentIdField, gbc);
        gbc.gridx = 2;
        JButton loadBtn = new JButton("Load Student");
        loadBtn.addActionListener(e -> loadStudent());
        formPanel.add(loadBtn, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        formPanel.add(new JLabel("Student Name:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        newStudentNameField = new JTextField(15);
        formPanel.add(newStudentNameField, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 3; gbc.gridx = 0;
        formPanel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        newPasswordField = new JPasswordField(15);
        formPanel.add(newPasswordField, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 4; gbc.gridx = 1; gbc.gridwidth = 2;
        JButton updateInfoBtn = new JButton("Save Student Info");
        updateInfoBtn.addActionListener(e -> updateStudentInfo());
        formPanel.add(updateInfoBtn, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 3;
        formPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // -- Update Course Section
        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 3;
        formPanel.add(new JLabel("Change a Course"), gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 7; gbc.gridx = 0;
        formPanel.add(new JLabel("Current Course:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        existingCourseCombo = new JComboBox<>();
        formPanel.add(existingCourseCombo, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 8; gbc.gridx = 0;
        formPanel.add(new JLabel("Replace With:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        newCourseCombo = new JComboBox<>();
        formPanel.add(newCourseCombo, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 9; gbc.gridx = 1; gbc.gridwidth = 2;
        updateCourseBtn = new JButton("Update Course");
        updateCourseBtn.setEnabled(false);
        updateCourseBtn.addActionListener(e -> updateCourse());
        formPanel.add(updateCourseBtn, gbc);
        gbc.gridwidth = 1;

        // Push components to the top left
        gbc.gridy = 10; gbc.gridx = 0; gbc.weighty = 1.0;
        formPanel.add(Box.createVerticalGlue(), gbc);
        gbc.gridy = 0; gbc.gridx = 3; gbc.weightx = 1.0; gbc.weighty = 0;
        formPanel.add(Box.createHorizontalGlue(), gbc);

        JScrollPane scroll = new JScrollPane(formPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        loadAllCourses();
        searchStudentIdField.addActionListener(e -> loadStudent());
    }

    private void loadStudent() {
        String sid = searchStudentIdField.getText().trim();
        if (sid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a Student ID to load.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Registration> regs = registrationDAO.getStudentRegistrations(sid);

        existingCourseCombo.removeAllItems();

        if (regs.isEmpty()) {
            currentlyLoadedName = "";
            newStudentNameField.setText("");
            existingCourseCombo.addItem("— No registered courses —");
            existingCourseCombo.setEnabled(false);
            if (updateCourseBtn != null) updateCourseBtn.setEnabled(false);
        } else {
            // RBAC check: if student role, verify this record belongs to them
            if ("student".equalsIgnoreCase(currentUser.getRole())) {
                String loadedName = regs.get(0).getStudentName();
                if (!loadedName.equalsIgnoreCase(currentUser.getUsername())) {
                    JOptionPane.showMessageDialog(this,
                        "Access Denied! You can only update your own details.\nLoaded ID belongs to: " + loadedName,
                        "Access Denied", JOptionPane.ERROR_MESSAGE);
                    searchStudentIdField.setText("");
                    newStudentNameField.setText("");
                    existingCourseCombo.addItem("— Load your own Student ID —");
                    existingCourseCombo.setEnabled(false);
                    if (updateCourseBtn != null) updateCourseBtn.setEnabled(false);
                    return;
                }
            }
            currentlyLoadedName = regs.get(0).getStudentName();
            newStudentNameField.setText(currentlyLoadedName);
            for (Registration r : regs) {
                existingCourseCombo.addItem(r.getCourseId() + " - " + r.getCourseName());
            }
            existingCourseCombo.setEnabled(true);
            if (updateCourseBtn != null) updateCourseBtn.setEnabled(true);
        }
    }

    private void loadAllCourses() {
        newCourseCombo.removeAllItems();
        List<Course> all = courseDAO.getAllCourses();
        for (Course c : all) {
            newCourseCombo.addItem(c.getCourseId() + " - " + c.getCourseName());
        }
    }

    private void updateStudentInfo() {
        String currentId = searchStudentIdField.getText().trim();
        String newName   = newStudentNameField.getText().trim();
        String newPass   = new String(newPasswordField.getPassword()).trim();

        if (currentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Load a student first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // RBAC: block students from updating another student's info
        if ("student".equalsIgnoreCase(currentUser.getRole())) {
            if (!currentlyLoadedName.equalsIgnoreCase(currentUser.getUsername())) {
                JOptionPane.showMessageDialog(this,
                    "Access Denied! You can only update your own details.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        boolean changed = false;

        if (!newName.isEmpty() && !newName.equals(currentlyLoadedName)) {
            if (!registrationDAO.updateStudentName(currentId, newName)) {
                JOptionPane.showMessageDialog(this, "Failed to update Student Name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!userDAO.updateUsername(currentUser.getUsername(), newName)) {
                JOptionPane.showMessageDialog(this, "Note: username update failed. Only registration name updated.", "Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                currentUser.setUsername(newName);
            }
            changed = true;
            currentlyLoadedName = newName;
        }

        if (!newPass.isEmpty()) {
            if (!userDAO.updatePassword(currentUser.getUsername(), newPass)) {
                JOptionPane.showMessageDialog(this, "Failed to update password.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            changed = true;
        }

        if (changed) {
            JOptionPane.showMessageDialog(this, "Student information updated!", "Updated", JOptionPane.INFORMATION_MESSAGE);
            newStudentNameField.setText(""); newPasswordField.setText("");
            loadStudent();
        } else {
            JOptionPane.showMessageDialog(this, "Nothing changed.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateCourse() {
        String sid    = searchStudentIdField.getText().trim();
        String oldSel = (String) existingCourseCombo.getSelectedItem();
        String newSel = (String) newCourseCombo.getSelectedItem();

        if (sid.isEmpty() || oldSel == null || newSel == null) {
            JOptionPane.showMessageDialog(this, "Load a student and select courses.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!oldSel.contains(" - ")) { JOptionPane.showMessageDialog(this, "Select a valid current course.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        if (!newSel.contains(" - ")) { JOptionPane.showMessageDialog(this, "Select a valid replacement course.", "Error", JOptionPane.ERROR_MESSAGE); return; }

        // RBAC: block students from updating another student's courses
        if ("student".equalsIgnoreCase(currentUser.getRole())) {
            if (!currentlyLoadedName.equalsIgnoreCase(currentUser.getUsername())) {
                JOptionPane.showMessageDialog(this,
                    "Access Denied! You can only update your own courses.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String oldId = oldSel.split(" - ")[0].trim();
        String newId = newSel.split(" - ")[0].trim();

        if (oldId.equals(newId)) {
            JOptionPane.showMessageDialog(this, "Select a different course to replace with.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (registrationDAO.updateRegistrationCourse(sid, oldId, newId)) {
            JOptionPane.showMessageDialog(this, "Course updated successfully!", "Updated", JOptionPane.INFORMATION_MESSAGE);
            loadStudent();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update course. It may already be registered.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
