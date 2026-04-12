package gui;

import app.AppController;
import app.DataAccess;
import ref.Instructor;
import ref.LeaveRequest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LeaveRequestPanel extends BasePanel {

    private final DataAccess db;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterCombo;
    private JComboBox<String> valueCombo;
    private List<Instructor> instructors;

    public LeaveRequestPanel(AppController controller, DataAccess db) {
        super(controller);
        this.db = db;
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.topBar("Leave Requests", ""), BorderLayout.NORTH);

        // ── Filter bar ─────────────────────────────────────────────────────────
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(UIHelper.BG);
        filterBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIHelper.BORDER));

        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(UIHelper.FONT_LABEL);

        filterCombo = new JComboBox<>(new String[]{"All", "By Status", "By Instructor"});
        filterCombo.setFont(UIHelper.FONT_SUB);
        filterCombo.setPreferredSize(new Dimension(145, 30));

        valueCombo = new JComboBox<>();
        valueCombo.setFont(UIHelper.FONT_SUB);
        valueCombo.setPreferredSize(new Dimension(190, 30));
        valueCombo.setVisible(false);

        JButton applyBtn = UIHelper.button("Apply");
        applyBtn.setPreferredSize(new Dimension(90, 30));

        filterCombo.addActionListener(e -> updateValueCombo());
        applyBtn.addActionListener(e -> applyFilter());

        filterBar.add(filterLabel);
        filterBar.add(filterCombo);
        filterBar.add(valueCombo);
        filterBar.add(applyBtn);

        // ── Table ──────────────────────────────────────────────────────────────
        String[] cols = {"Request ID", "Instructor ID", "Leave Type", "Start Date", "End Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0);
        JTable table = UIHelper.makeTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(UIHelper.BG);
        body.setBorder(BorderFactory.createEmptyBorder(0, 18, 10, 18));
        body.add(filterBar, BorderLayout.NORTH);
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
        add(bottomBar(), BorderLayout.SOUTH);

        loadRequests(db.getAllLeaveRequests());
    }

    private void updateValueCombo() {
        String sel = (String) filterCombo.getSelectedItem();
        valueCombo.removeAllItems();
        if ("By Status".equals(sel)) {
            for (String s : new String[]{"Pending", "Approved", "Unauthorized"})
                valueCombo.addItem(s);
            valueCombo.setVisible(true);
        } else if ("By Instructor".equals(sel)) {
            instructors = db.getAllInstructors();
            for (Instructor i : instructors) valueCombo.addItem(i.getName());
            valueCombo.setVisible(true);
        } else {
            valueCombo.setVisible(false);
        }
    }

    private void applyFilter() {
        String mode = (String) filterCombo.getSelectedItem();
        if ("By Status".equals(mode)) {
            String status = (String) valueCombo.getSelectedItem();
            if (status != null) loadRequests(db.getLeaveRequestsByStatus(status));
        } else if ("By Instructor".equals(mode)) {
            int idx = valueCombo.getSelectedIndex();
            if (idx >= 0 && instructors != null)
                loadRequests(db.getLeaveRequestsByInstructor(instructors.get(idx).getInstructorID()));
        } else {
            loadRequests(db.getAllLeaveRequests());
        }
    }

    private void loadRequests(List<LeaveRequest> list) {
        tableModel.setRowCount(0);
        for (LeaveRequest lr : list) {
            tableModel.addRow(new Object[]{
                    lr.getLeaveReqID(),
                    lr.getInstructID(),
                    lr.getLeaveType(),
                    lr.getStartDate(),
                    lr.getEndDate(),
                    lr.getStatus()
            });
        }
    }
}