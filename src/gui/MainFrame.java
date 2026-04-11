package gui;

import app.AppController;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final AppController controller;
    private final JPanel contentArea;

    public MainFrame(AppController controller) {
        this.controller = controller;
        setTitle("Attendance & Leave Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 640);
        setMinimumSize(new Dimension(720, 480));
        setLocationRelativeTo(null);
        contentArea = new JPanel(new BorderLayout());
        setContentPane(contentArea);
    }

    public void showPanel(JPanel panel) {
        contentArea.removeAll();
        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public AppController getController() { return controller; }
}