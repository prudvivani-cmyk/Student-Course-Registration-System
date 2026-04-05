package ui;

import models.User;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {

    private final User currentUser;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    public DashboardFrame(User user) {
        currentUser = user;
        setTitle("Dashboard - " + user.getUsername() + " (" + user.getRole() + ")");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        initUI();
        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.add(new JLabel("Student Course Registration System | Logged in: " + currentUser.getUsername()));
        add(headerPanel, BorderLayout.NORTH);

        // Sidebar
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        CourseRegistrationPanel regPanel = new CourseRegistrationPanel(currentUser);
        ViewStudentPanel viewPanel       = new ViewStudentPanel(currentUser);
        SearchStudentPanel searchPanel   = new SearchStudentPanel(currentUser);
        UpdateRegistrationPanel updatePanel = new UpdateRegistrationPanel(currentUser);
        DeleteRegistrationPanel deletePanel = new DeleteRegistrationPanel(currentUser);

        mainContentPanel.add(regPanel,    "CourseRegistration");
        mainContentPanel.add(viewPanel,   "ViewStudent");
        mainContentPanel.add(searchPanel, "SearchStudent");
        mainContentPanel.add(updatePanel, "UpdateRegistration");
        mainContentPanel.add(deletePanel, "DeleteRegistration");

        String[][] menuItems = {
            {"Course Registration",    "CourseRegistration"},
            {"View Students",         "ViewStudent"},
            {"Search Student",         "SearchStudent"},
            {"Update Registration",   "UpdateRegistration"},
            {"Delete Registration",   "DeleteRegistration"},
        };

        for (String[] item : menuItems) {
            JButton btn = new JButton(item[0]);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            btn.addActionListener(e -> {
                cardLayout.show(mainContentPanel, item[1]);
                if ("ViewStudent".equals(item[1])) {
                    viewPanel.loadSummary();
                }
            });
            navPanel.add(btn);
            navPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        navPanel.add(Box.createVerticalGlue());

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logoutBtn.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
        navPanel.add(logoutBtn);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, navPanel, mainContentPanel);
        splitPane.setDividerLocation(180);
        add(splitPane, BorderLayout.CENTER);
    }
}