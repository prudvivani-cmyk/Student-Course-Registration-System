package ui;

import dao.RegistrationDAO;
import models.Registration;
import models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SearchStudentPanel extends JPanel {

    private JTextField studentIdField;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private RegistrationDAO registrationDAO;
    
    private JLabel summaryLabel;

    public SearchStudentPanel(User currentUser) {
        
        this.registrationDAO = new RegistrationDAO();
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel title = new JLabel("Search Student");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(title);
        topPanel.add(headerPanel);
        topPanel.add(Box.createVerticalStrut(14));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        searchBar.add(new JLabel("Student ID:"));

        studentIdField = new JTextField(15);
        searchBar.add(studentIdField);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> searchStudent());
        searchBar.add(searchBtn);

        summaryLabel = new JLabel(" ");
        searchBar.add(summaryLabel);

        topPanel.add(searchBar);
        add(topPanel, BorderLayout.NORTH);

        studentIdField.addActionListener(e -> searchStudent());

        String[] cols = {"Course ID", "Course Name", "Semester", "Credits", "Registration Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        resultTable = new JTable(tableModel);
        resultTable.setRowHeight(28);
        resultTable.setIntercellSpacing(new Dimension(10, 6));
        JScrollPane scrollPane = new JScrollPane(resultTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 20));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void searchStudent() {
        String studentId = studentIdField.getText().trim();
        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Student ID to search.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Registration> registrations;
        try {
            registrations = registrationDAO.getStudentRegistrations(studentId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Search failed: " + e.getMessage() + ". Check database connection.", "Database Error", JOptionPane.ERROR_MESSAGE);
            summaryLabel.setText("Search error.");
            summaryLabel.setForeground(Color.RED);
            return;
        }

        tableModel.setRowCount(0);

        if (registrations.isEmpty()) {
            summaryLabel.setText("No registrations found.");
            summaryLabel.setForeground(Color.RED);
        } else {
            String name = registrations.get(0).getStudentName();
            summaryLabel.setText("Student: " + name + " | ID: " + studentId + " | Total Courses: " + registrations.size());
            summaryLabel.setForeground(new Color(0, 150, 0));

            for (Registration reg : registrations) {
                tableModel.addRow(new Object[]{
                    reg.getCourseId(),
                    reg.getCourseName(),
                    "Semester " + reg.getSemester(),
                    reg.getCredits(),
                    reg.getRegistrationDate()
                });
            }
        }
    }
}
