package gui;

import app.AppController;
import app.DataAccess;
import ref.Department;
import ref.SystemUser;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CreateUserPanel extends BasePanel {

    private final DataAccess db;
    private JTextField nameField, userField, emailField;
    private JPasswordField passField;
    private JComboBox<String> roleCombo;
    private JComboBox<Department> deptCombo;
    private JTextField extraField;
    private JLabel extraLabel;

    public CreateUserPanel(AppController controller, DataAccess db) {
        super(controller);
        this.db = db;
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.topBar("User Management", "Create New System Account"), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIHelper.BG);
        form.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        addFormField(form, "Full Name:", nameField = new JTextField(20), gbc, 0);
        addFormField(form, "Username:", userField = new JTextField(20), gbc, 1);
        addFormField(form, "Email:", emailField = new JTextField(20), gbc, 2);
        addFormField(form, "Password:", passField = new JPasswordField(20), gbc, 3);

        roleCombo = new JComboBox<>(new String[]{"Secretary", "DeptHead", "Checker", "Admin"});
        addFormField(form, "User Role:", roleCombo, gbc, 4);

        deptCombo = new JComboBox<>();
        deptCombo.setFont(UIHelper.FONT_SUB);
        deptCombo.setPreferredSize(new Dimension(190, 30));

        List<Department> depts = db.getDepartments();
        for (Department d : depts) {
            deptCombo.addItem(d);
        }

        extraLabel = new JLabel("Department:");
        extraLabel.setFont(UIHelper.FONT_LABEL);
        extraField = new JTextField(20);

        gbc.gridy = 5;
        gbc.gridx = 0;
        form.add(extraLabel, gbc);
        gbc.gridx = 1;
        form.add(deptCombo, gbc);
        form.add(extraField, gbc);

        roleCombo.addActionListener(e -> updateExtraFields());
        updateExtraFields();

        add(new JScrollPane(form), BorderLayout.CENTER);

        JButton saveBtn = UIHelper.button("Create Account");
        saveBtn.addActionListener(e -> handleSave());

        JButton cancelBtn = UIHelper.secondaryButton("Cancel");
        cancelBtn.addActionListener(e -> controller.showAccountList());

        add(bottomBar(cancelBtn, saveBtn), BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, String labelText, JComponent comp, GridBagConstraints gbc, int y) {
        gbc.gridy = y;
        gbc.gridx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(UIHelper.FONT_LABEL);
        panel.add(label, gbc);
        gbc.gridx = 1;
        panel.add(comp, gbc);
    }

    private void updateExtraFields() {
        String role = (String) roleCombo.getSelectedItem();

        deptCombo.setVisible(false);
        extraField.setVisible(false);

        if ("Secretary".equals(role) || "DeptHead".equals(role)) {
            extraLabel.setText("Department:");
            deptCombo.setVisible(true);
        } else if ("Checker".equals(role)) {
            extraLabel.setText("Floor Assignment:");
            extraField.setVisible(true);
            extraField.setText("");
        } else if ("Admin".equals(role)) {
            extraLabel.setText("Approval Code:");
            extraField.setVisible(true);
            extraField.setText("ADMIN-2026");
        }
        revalidate();
        repaint();
    }

    private void handleSave() {
        String role = (String) roleCombo.getSelectedItem();

        if (userField.getText().isBlank() || String.valueOf(passField.getPassword()).isBlank()) {
            JOptionPane.showMessageDialog(this, "Username and Password are required.");
            return;
        }

        SystemUser newUser = new SystemUser(
                0,
                nameField.getText(),
                userField.getText(),
                emailField.getText(),
                new String(passField.getPassword()),
                role,
                null
        );

        Object extra = null;
        try {
            if ("Secretary".equals(role) || "DeptHead".equals(role)) {

                Department d = (Department) deptCombo.getSelectedItem();
                extra = (d != null) ? d.getDepartmentID() : null;
            } else if ("Checker".equals(role)) {
                extra = Integer.parseInt(extraField.getText());
            } else if ("Admin".equals(role)) {
                extra = extraField.getText();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for the Floor.");
            return;
        }

        controller.createUser(newUser, extra);
    }
}