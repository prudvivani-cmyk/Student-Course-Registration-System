package ui;

import dao.RegistrationDAO;
import models.Registration;
import models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DeleteRegistrationPanel extends JPanel {

    private RegistrationDAO registrationDAO;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JTextField studentIdField;
    private JLabel statusLabel;
    private User currentUser;
    // Tracks the name of the student whose data is currently loaded in the table
    private String currentlyLoadedName = "";

    public DeleteRegistrationPanel(User currentUser) {
        this.currentUser = currentUser;
        this.registrationDAO = new RegistrationDAO();

        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel title = new JLabel("Delete Course Registration");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.RED);
        headerPanel.add(title);
        topPanel.add(headerPanel);
        topPanel.add(Box.createVerticalStrut(14));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        searchBar.add(new JLabel("Student ID:"));

        studentIdField = new JTextField(15);
        searchBar.add(studentIdField);

        JButton loadBtn = new JButton("Load Registrations");
        loadBtn.addActionListener(e -> loadRegistrations());
        searchBar.add(loadBtn);

        statusLabel = new JLabel(" ");
        searchBar.add(statusLabel);

        topPanel.add(searchBar);
        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Select", "Course ID", "Course Name", "Semester", "Registration Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
            @Override
            public boolean isCellEditable(int r, int c) { return c == 0; }
        };

        resultTable = new JTable(tableModel);
        resultTable.setRowHeight(28);
        resultTable.setIntercellSpacing(new Dimension(10, 6));
        JScrollPane scrollPane = new JScrollPane(resultTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        bottomBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));
        JCheckBox selectAllBox = new JCheckBox("Select All");
        selectAllBox.addActionListener(e -> {
            boolean sel = selectAllBox.isSelected();
            for (int i = 0; i < tableModel.getRowCount(); i++) tableModel.setValueAt(sel, i, 0);
        });

        JButton deleteSelectedBtn = new JButton("Delete Selected");
        deleteSelectedBtn.addActionListener(e -> deleteSelected());

        JButton deleteAllBtn = new JButton("Delete All Registrations");
        deleteAllBtn.addActionListener(e -> deleteAll());

        bottomBar.add(selectAllBox);
        bottomBar.add(deleteSelectedBtn);
        bottomBar.add(deleteAllBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void loadRegistrations() {
        String sid = studentIdField.getText().trim();
        if (sid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Student ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Registration> regs = registrationDAO.getStudentRegistrations(sid);

        tableModel.setRowCount(0);
        currentlyLoadedName = "";

        if (regs.isEmpty()) {
            statusLabel.setText("No registrations found.");
            statusLabel.setForeground(Color.RED);
        } else {
            // RBAC: students can only VIEW; delete will be blocked later if it's not their data
            // But show a warning immediately so the student knows
            String loadedName = regs.get(0).getStudentName();
            currentlyLoadedName = loadedName;

            if ("student".equalsIgnoreCase(currentUser.getRole()) &&
                !loadedName.equalsIgnoreCase(currentUser.getUsername())) {
                statusLabel.setText("Access Denied — this is not your record.");
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this,
                    "Access Denied! You can only delete your own registrations.\nThis ID belongs to: " + loadedName,
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
                studentIdField.setText("");
                return;
            }

            statusLabel.setText("Loaded: " + loadedName + " | " + regs.size() + " courses");
            statusLabel.setForeground(new Color(0, 150, 0));
            for (Registration r : regs) {
                String dateStr = (r.getRegistrationDate() != null) ? r.getRegistrationDate().toString() : "N/A";
                tableModel.addRow(new Object[]{
                    false,
                    r.getCourseId(),
                    r.getCourseName(),
                    "Semester " + r.getSemester(),
                    dateStr
                });
            }
        }
    }

    private void deleteSelected() {
        String sid = studentIdField.getText().trim();
        if (sid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please load a student first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // RBAC check at deletion time
        if ("student".equalsIgnoreCase(currentUser.getRole())) {
            if (!currentlyLoadedName.equalsIgnoreCase(currentUser.getUsername())) {
                JOptionPane.showMessageDialog(this,
                    "Access Denied! You can only delete your own registrations.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        boolean any = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean sel = (Boolean) tableModel.getValueAt(i, 0);
            if (sel != null && sel) {
                String cid = (String) tableModel.getValueAt(i, 1);
                if (registrationDAO.deleteRegistration(sid, cid)) {
                    any = true;
                }
            }
        }
        if (any) {
            JOptionPane.showMessageDialog(this, "Selected registrations deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            loadRegistrations();
        } else {
            JOptionPane.showMessageDialog(this, "Please select at least one course to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteAll() {
        String sid = studentIdField.getText().trim();
        if (sid.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please load a student first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // RBAC check at deletion time
        if ("student".equalsIgnoreCase(currentUser.getRole())) {
            if (!currentlyLoadedName.equalsIgnoreCase(currentUser.getUsername())) {
                JOptionPane.showMessageDialog(this,
                    "Access Denied! You can only delete your own registrations.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete ALL course registrations\nfor Student ID: " + sid + "?\n\nThis cannot be undone.",
            "Confirm Delete All", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (registrationDAO.deleteAllRegistrations(sid)) {
                JOptionPane.showMessageDialog(this, "All registrations deleted.", "Done", JOptionPane.INFORMATION_MESSAGE);
                tableModel.setRowCount(0);
                currentlyLoadedName = "";
                statusLabel.setText("All registrations cleared.");
            } else {
                JOptionPane.showMessageDialog(this, "No registrations found or deletion failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
