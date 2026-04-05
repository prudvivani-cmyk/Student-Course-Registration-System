package ui;

import dao.RegistrationDAO;
import models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Dimension;
import java.util.List;

public class ViewStudentPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private RegistrationDAO registrationDAO;
    private JLabel totalLabel;

    public ViewStudentPanel(User currentUser) {
        this.registrationDAO = new RegistrationDAO();
        setLayout(new BorderLayout());
        initUI();
        loadSummary();
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20));
        JLabel title = new JLabel("Registered Students Overview");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadSummary());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        String[] cols = {"Student ID", "Student Name", "Courses Registered"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setIntercellSpacing(new Dimension(10, 6));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));
        totalLabel = new JLabel("Total Students: 0");
        totalLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        bottomPanel.add(totalLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void loadSummary() {
        tableModel.setRowCount(0);
        List<Object[]> rows = registrationDAO.getStudentSummary();
        for (Object[] row : rows) {
            tableModel.addRow(new Object[]{
                row[0],   
                row[1],   
                row[2] + " course(s)"  
            });
        }
        if (rows.isEmpty()) {
            totalLabel.setText("No students registered yet.");
        } else {
            totalLabel.setText("Total Students: " + rows.size());
        }
    }
}
