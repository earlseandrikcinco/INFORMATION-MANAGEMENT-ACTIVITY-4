package gui;

import app.AppController;
import app.DataAccess;
import ref.SystemUser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AccountListPanel extends BasePanel {

    private final DataAccess db;

    public AccountListPanel(AppController controller, DataAccess db) {
        super(controller);
        this.db = db;
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.topBar("All Accounts", "Admin · Read-Only"), BorderLayout.NORTH);

        List<SystemUser> users = db.getAllAccounts();

        String[] cols = {"User ID", "Name", "Username", "Email", "Role"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (SystemUser u : users) {
            model.addRow(new Object[]{
                    u.getUserID(), u.getName(), u.getUsername(), u.getEmail(), u.getRole()
            });
        }

        JTable table = UIHelper.makeTable(model);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);

        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setBackground(UIHelper.BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));
        body.add(UIHelper.sub("Total accounts: " + users.size()), BorderLayout.NORTH);
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
        add(bottomBar(), BorderLayout.SOUTH);
    }
}