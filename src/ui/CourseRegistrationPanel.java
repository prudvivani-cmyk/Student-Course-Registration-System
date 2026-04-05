package ui;

import dao.CourseDAO;
import dao.RegistrationDAO;
import models.Course;
import models.Registration;
import models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CourseRegistrationPanel extends JPanel {

    private JTextField studentIdField;
    private JTextField studentNameField;
    private JComboBox<String> semesterComboBox;
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JCheckBox selectAllCheckBox;
    private CourseDAO courseDAO;
    private RegistrationDAO registrationDAO;
    private User currentUser;

    public CourseRegistrationPanel(User currentUser) {
        this.currentUser = currentUser;
        this.courseDAO = new CourseDAO();
        this.registrationDAO = new RegistrationDAO();

        setLayout(new BorderLayout());
        initUI();
        applyRBAC();
        loadCourses();
    }

    private void applyRBAC() {
        if ("student".equalsIgnoreCase(currentUser.getRole())) {
            studentNameField.setText(currentUser.getUsername());
            
            List<Registration> regs = registrationDAO.getStudentRegistrations(currentUser.getUsername());
            if (!regs.isEmpty()) {
                studentIdField.setText(regs.get(0).getStudentId());
                studentIdField.setEditable(false);
            }
        }
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Course Registration"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        studentIdField = new JTextField(20);
        topPanel.add(studentIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        topPanel.add(new JLabel("Student Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        studentNameField = new JTextField(20);
        topPanel.add(studentNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        topPanel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        semesterComboBox = new JComboBox<>(new String[]{"Semester 1", "Semester 2", "Semester 3", "Semester 4"});
        semesterComboBox.addActionListener(e -> loadCourses());
        topPanel.add(semesterComboBox, gbc);

        // Add glue to prevent stretching across full screen
        gbc.gridx = 2; gbc.weightx = 5.0;
        topPanel.add(Box.createHorizontalGlue(), gbc);

        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Select", "Course ID", "Course Name", "Credits", "Price (₹)", "Reg. Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 ? Boolean.class : String.class;
            }
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 0;
            }
        };

        courseTable = new JTable(tableModel);
        add(new JScrollPane(courseTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        selectAllCheckBox = new JCheckBox("Select All");
        selectAllCheckBox.addActionListener(e -> {
            boolean sel = selectAllCheckBox.isSelected();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                tableModel.setValueAt(sel, i, 0);
            }
        });
        bottomPanel.add(selectAllCheckBox);

        JButton registerBtn = new JButton("Register Selected Courses");
        registerBtn.addActionListener(e -> registerSelectedCourses());
        bottomPanel.add(registerBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadCourses() {
        int semester = semesterComboBox.getSelectedIndex() + 1;
        List<Course> courses = courseDAO.getCoursesBySemester(semester);
        tableModel.setRowCount(0);
        selectAllCheckBox.setSelected(false);
        String today = LocalDate.now().toString();
        for (Course c : courses) {
            tableModel.addRow(new Object[]{false, c.getCourseId(), c.getCourseName(),
                String.valueOf(c.getCredits()), "₹500", today});
        }
    }

    private void registerSelectedCourses() {
        String studentId = studentIdField.getText().trim();
        String studentName = studentNameField.getText().trim();

        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Student ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidRollNumber(studentId)) {
            JOptionPane.showMessageDialog(this, "Invalid Student ID format. Example: 24B11CS001", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (studentName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Student Name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("student".equalsIgnoreCase(currentUser.getRole())) {
            List<Registration> existing = registrationDAO.getStudentRegistrations(studentId);
            if (!existing.isEmpty() && !existing.get(0).getStudentName().equalsIgnoreCase(currentUser.getUsername())) {
                JOptionPane.showMessageDialog(this, "Unauthorized! This Student ID is already registered to another student.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Use the name entered in the field (which is auto-populated with username but can be modified)
            studentName = studentNameField.getText().trim();
        }

        List<String> selectedIds = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean sel = (Boolean) tableModel.getValueAt(i, 0);
            if (sel != null && sel) selectedIds.add((String) tableModel.getValueAt(i, 1));
        }

        if (selectedIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one course to register.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if student already has 6 courses registered
        int currentCourseCount = registrationDAO.getRegistrationCount(studentId);
        if (currentCourseCount >= 6) {
            JOptionPane.showMessageDialog(this, "Student already has 6 courses registered. No more courses can be added.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if adding selected courses would exceed limit
        int totalAfterRegistration = currentCourseCount + selectedIds.size();
        if (totalAfterRegistration > 6) {
            int allowedCourses = 6 - currentCourseCount;
            JOptionPane.showMessageDialog(this, "Student can only register " + allowedCourses + " more course(s) (max 6 total).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int inserted = registrationDAO.registerMultipleCourses(studentId, studentName, selectedIds);
        int alreadyHad = selectedIds.size() - Math.max(inserted, 0);

        if (inserted == -1) {
            JOptionPane.showMessageDialog(this, "Registration failed due to a database error.", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (inserted == 0 && alreadyHad > 0) {
            JOptionPane.showMessageDialog(this, "Selected courses are already registered.", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else if (alreadyHad > 0) {
            JOptionPane.showMessageDialog(this, "Registered " + inserted + " new courses. " + alreadyHad + " skipped.", "Info", JOptionPane.WARNING_MESSAGE);
            resetSelection();
        } else {
            JOptionPane.showMessageDialog(this, "Successfully registered " + inserted + " courses!", "Success", JOptionPane.INFORMATION_MESSAGE);
            resetSelection();
        }
    }

    private void resetSelection() {
        selectAllCheckBox.setSelected(false);
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(false, i, 0);
        }
    }

    private static boolean isValidRollNumber(String studentId) {
        Pattern pattern = Pattern.compile("^24[A-Z]\\d{2}CS\\d{3}$", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(studentId).matches();
    }
}
