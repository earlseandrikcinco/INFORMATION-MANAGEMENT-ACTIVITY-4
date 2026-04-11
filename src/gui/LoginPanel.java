package gui;

import app.AppController;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {

    private final AppController controller;
    private JTextField userField;
    private JPasswordField passField;
    private JLabel errorLabel;

    public LoginPanel(AppController controller) {
        this.controller = controller;
        setBackground(UIHelper.BG);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel card = new JPanel();
        card.setBackground(UIHelper.SURFACE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(36, 44, 36, 44));

        JLabel title = UIHelper.title("Attendance & Leave System");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = UIHelper.sub("Sign in to continue");
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel uLabel = new JLabel("Username");
        uLabel.setFont(UIHelper.FONT_LABEL);
        uLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        userField = new JTextField(18);
        userField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        userField.setFont(UIHelper.FONT_SUB);
        userField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel pLabel = new JLabel("Password");
        pLabel.setFont(UIHelper.FONT_LABEL);
        pLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passField = new JPasswordField(18);
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        passField.setFont(UIHelper.FONT_SUB);
        passField.setAlignmentX(Component.LEFT_ALIGNMENT);

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = UIHelper.button("Log In");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));
        card.add(uLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(userField);
        card.add(Box.createVerticalStrut(12));
        card.add(pLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(passField);
        card.add(Box.createVerticalStrut(6));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(14));
        card.add(loginBtn);

        add(card);

        ActionListener doLogin = e -> attemptLogin();
        loginBtn.addActionListener(doLogin);
        passField.addActionListener(doLogin);
    }

    private void attemptLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }
        errorLabel.setText(" ");
        controller.login(user, pass);
        // If login failed, currentUser is still null
        if (controller.getCurrentUser() == null) {
            errorLabel.setText("Invalid username or password.");
            passField.setText("");
        }
    }
}