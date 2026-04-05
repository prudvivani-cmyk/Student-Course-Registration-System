package main;

import ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            // Set global font size for Nimbus
            UIManager.getLookAndFeelDefaults().put("defaultFont", new java.awt.Font("Dialog", java.awt.Font.PLAIN, 15));
            UIManager.put("Button.font", new java.awt.Font("Dialog", java.awt.Font.BOLD, 15));
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
    
}

