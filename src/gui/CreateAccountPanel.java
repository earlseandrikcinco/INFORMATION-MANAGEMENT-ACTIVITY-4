package gui;

import app.AppController;
import app.DataAccess;
import ref.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Admin panel for creating new system user accounts.
 * Handles all four roles: Checker, Secretary, DeptHead, Admin.
 * Role-specific extra field (floor / departmentID / approvalCode) appears
 * dynamically once a role is selected.
 */
public class CreateAccountPanel extends BasePanel {

    private final DataAccess db;

    private JTextField nameField;
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> roleCombo;

    private JPanel extraPanel;
    private JLabel extraLabel;
    private JTextField floorField;
    private JComboBox<Department> deptCombo;
    private JTextField approvalCodeField;
    private List<Department> departments;
    private final int adminID;

    public CreateAccountPanel(AppController controller, DataAccess db, Admin admin) {
        super(controller);
        this.db = db;
        this.adminID = admin.getUserID();
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.topBar("Create Account", "Admin"), BorderLayout.NORTH);

        JPanel card = new JPanel();
        card.setBackground(UIHelper.SURFACE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 32, 24, 32));
        card.setMaximumSize(new Dimension(480, Integer.MAX_VALUE));

        nameField = new JTextField();
        usernameField = new JTextField();
        emailField = new JTextField();
        passwordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();
        roleCombo = new JComboBox<>(new String[]{"— Select Role —", "Checker", "Secretary", "DeptHead", "Admin"});
        roleCombo.setFont(UIHelper.FONT_SUB);

        card.add(formRow("Full Name", nameField));
        card.add(Box.createVerticalStrut(10));
        card.add(formRow("Username", usernameField));
        card.add(Box.createVerticalStrut(10));
        card.add(formRow("Email", emailField));
        card.add(Box.createVerticalStrut(10));
        card.add(formRow("Password", passwordField));
        card.add(Box.createVerticalStrut(10));
        card.add(formRow("Confirm Password", confirmPasswordField));
        card.add(Box.createVerticalStrut(10));
        card.add(formRow("Role", roleCombo));
        card.add(Box.createVerticalStrut(10));

        extraPanel = new JPanel(new BorderLayout(8, 0));
        extraPanel.setOpaque(false);
        extraPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        extraLabel = new JLabel("");
        extraLabel.setFont(UIHelper.FONT_LABEL);
        extraLabel.setPreferredSize(new Dimension(130, 30));

        floorField = new JTextField();
        floorField.setFont(UIHelper.FONT_SUB);

        departments = db.getDepartments();
        deptCombo = new JComboBox<>();
        deptCombo.setFont(UIHelper.FONT_SUB);
        for (Department d : departments) deptCombo.addItem(d);

        approvalCodeField = new JTextField();
        approvalCodeField.setFont(UIHelper.FONT_SUB);

        extraPanel.add(extraLabel, BorderLayout.WEST);
        extraPanel.setVisible(false);
        card.add(extraPanel);
        card.add(Box.createVerticalStrut(4));

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(statusLabel);

        JButton submitBtn = UIHelper.button("Create Account");
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        card.add(Box.createVerticalStrut(10));
        card.add(submitBtn);

        roleCombo.addActionListener(e -> updateExtraField());

        submitBtn.addActionListener(e -> {
            statusLabel.setForeground(Color.RED);
            String err = validateForm();
            if (err != null) { statusLabel.setText(err); return; }
            boolean ok = submit();
            if (ok) {
                statusLabel.setForeground(new Color(0, 130, 0));
                statusLabel.setText("Account created successfully!");
                clearForm();
            } else {
                statusLabel.setText("Failed — username may already exist.");
            }
        });

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(UIHelper.BG);
        center.add(card);
        add(center, BorderLayout.CENTER);
        add(bottomBar(), BorderLayout.SOUTH);
    }

    private void updateExtraField() {
        String role = (String) roleCombo.getSelectedItem();
        extraPanel.removeAll();
        extraPanel.add(extraLabel, BorderLayout.WEST);

        switch (role == null ? "" : role) {
            case "Checker":
                extraLabel.setText("Floor #");
                floorField.setText("");
                extraPanel.add(floorField, BorderLayout.CENTER);
                extraPanel.setVisible(true);
                break;
            case "Secretary":
            case "DeptHead":
                extraLabel.setText("Department");
                extraPanel.add(deptCombo, BorderLayout.CENTER);
                extraPanel.setVisible(true);
                break;
            case "Admin":
                extraLabel.setText("Approval Code");
                approvalCodeField.setText("");
                extraPanel.add(approvalCodeField, BorderLayout.CENTER);
                extraPanel.setVisible(true);
                break;
            default:
                extraPanel.setVisible(false);
        }
        revalidate();
        repaint();
    }

    private String validateForm() {
        if (nameField.getText().isBlank())      return "Full name is required.";
        if (usernameField.getText().isBlank())  return "Username is required.";
        if (emailField.getText().isBlank())     return "Email is required.";
        String pw = new String(passwordField.getPassword());
        String pw2 = new String(confirmPasswordField.getPassword());
        if (pw.isBlank())                       return "Password is required.";
        if (!pw.equals(pw2))                    return "Passwords do not match.";
        String role = (String) roleCombo.getSelectedItem();
        if (role == null || role.startsWith("—")) return "Please select a role.";
        if ("Checker".equals(role)) {
            try { Integer.parseInt(floorField.getText().trim()); }
            catch (NumberFormatException ex) { return "Floor must be a number."; }
        }
        if ("Admin".equals(role) && approvalCodeField.getText().isBlank())
            return "Approval code is required.";
        return null;
    }

    private boolean submit() {
        String role = (String) roleCombo.getSelectedItem();
        int adminID = controller.getCurrentUser().getUserID();

        SystemUser user = new SystemUser(
                0,
                nameField.getText().trim(),
                usernameField.getText().trim(),
                emailField.getText().trim(),
                new String(passwordField.getPassword()),
                role,
                adminID
        );

        Object extra = switch (role) {
            case "Checker"   -> Integer.parseInt(floorField.getText().trim());
            case "Secretary",
                 "DeptHead"  -> ((Department) deptCombo.getSelectedItem()).getDepartmentID();
            case "Admin"     -> approvalCodeField.getText().trim();
            default          -> null;
        };

        return db.addSystemUser(user, extra, adminID);
    }

    private void clearForm() {
        nameField.setText("");
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        roleCombo.setSelectedIndex(0);
        floorField.setText("");
        approvalCodeField.setText("");
        extraPanel.setVisible(false);
    }

    private JPanel formRow(String labelText, JComponent input) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIHelper.FONT_LABEL);
        lbl.setPreferredSize(new Dimension(130, 30));
        if (input instanceof JTextField || input instanceof JPasswordField) {
            ((JComponent) input).setFont(UIHelper.FONT_SUB);
        }
        row.add(lbl, BorderLayout.WEST);
        row.add(input, BorderLayout.CENTER);
        return row;
    }
}