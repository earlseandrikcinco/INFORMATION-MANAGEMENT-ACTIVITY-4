package gui;

import app.AppController;
import javax.swing.*;
import java.awt.*;

public abstract class BasePanel extends JPanel {

    protected final AppController controller;

    public BasePanel(AppController controller) {
        this.controller = controller;
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout());
    }

    /** Bottom toolbar with a Back-to-Dashboard button, plus optional extras. */
    protected JPanel bottomBar(JButton... extraButtons) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setBackground(UIHelper.BG);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER));
        JButton back = UIHelper.secondaryButton("← Back");
        back.addActionListener(e -> controller.showDashboard());
        bar.add(back);
        for (JButton b : extraButtons) bar.add(b);
        return bar;
    }
}