package gui;

import app.AppController;
import javax.swing.*;
import java.awt.*;

public abstract class DashboardBase extends BasePanel {

    public DashboardBase(AppController controller) {
        super(controller);
    }

    protected void buildDashboard(String roleName, String userName, String[][] actions) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIHelper.ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        JLabel roleLabel = new JLabel(roleName + " Dashboard");
        roleLabel.setFont(UIHelper.FONT_TITLE);
        roleLabel.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JLabel nameLabel = new JLabel("Logged in as: " + userName);
        nameLabel.setFont(UIHelper.FONT_SUB);
        nameLabel.setForeground(new Color(200, 215, 255));
        JButton logoutBtn = UIHelper.secondaryButton("Log Out");
        logoutBtn.setPreferredSize(new Dimension(88, 28));
        logoutBtn.addActionListener(e -> controller.logout());
        right.add(nameLabel);
        right.add(logoutBtn);

        header.add(roleLabel, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setBackground(UIHelper.BG);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));

        JLabel prompt = UIHelper.sub("What would you like to do?");
        prompt.setAlignmentX(Component.CENTER_ALIGNMENT);
        menu.add(prompt);
        menu.add(Box.createVerticalStrut(20));

        for (String[] action : actions) {
            JButton btn = UIHelper.button(action[0]);
            btn.setPreferredSize(new Dimension(270, 42));
            btn.setMaximumSize(new Dimension(270, 42));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            final String cmd = action[1];
            btn.addActionListener(e -> handleAction(cmd));
            menu.add(btn);
            menu.add(Box.createVerticalStrut(12));
        }

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(UIHelper.BG);
        center.add(menu);
        add(center, BorderLayout.CENTER);
    }

    protected abstract void handleAction(String command);
}