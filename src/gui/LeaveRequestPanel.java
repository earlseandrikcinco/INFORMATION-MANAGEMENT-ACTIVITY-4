package gui;

import app.AppController;
import app.DataAccess;
import ref.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class LeaveRequestPanel extends BasePanel {

    private final DataAccess db;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterCombo;
    private JComboBox<String> valueCombo;
    private List<Instructor> instructors;
    private List<LeaveRequest> currentList = new ArrayList<>();
    private JTable table;
    private SystemUser currentUser;
    private int deptID;

    public LeaveRequestPanel(AppController controller, DataAccess db, SystemUser user) {
        super(controller);
        this.db = db;
        this.currentUser = user;
        if (currentUser instanceof Secretary secretary) {
            deptID = secretary.getDepartmentID();
        } else if (currentUser instanceof DeptHead deptHead) {
            deptID = deptHead.getDepartmentID();
        }
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.topBar("Leave Requests", ""), BorderLayout.NORTH);

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
        String[] cols = {"Request ID", "Instructor", "Leave Type", "Start Date", "End Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0);
        table = UIHelper.makeTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);

        // Double-click row opens the reason popup
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showReasonDialog();
            }
        });

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(UIHelper.BG);
        body.setBorder(BorderFactory.createEmptyBorder(0, 18, 10, 18));
        body.add(filterBar, BorderLayout.NORTH);
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);

        // "View Reason" button in the bottom bar
        JButton reasonBtn = UIHelper.button("View Reason");
        reasonBtn.addActionListener(e -> showReasonDialog());
        add(bottomBar(reasonBtn), BorderLayout.SOUTH);

        loadRequests(db.getLeaveRequestsByDept(deptID));
    }

    private void updateValueCombo() {
        String sel = (String) filterCombo.getSelectedItem();
        valueCombo.removeAllItems();
        if ("By Status".equals(sel)) {
            for (String s : new String[]{"Pending", "Approved", "Rejected"})
                valueCombo.addItem(s);
            valueCombo.setVisible(true);
        } else if ("By Instructor".equals(sel)) {
            instructors = db.getInstructorsByDept(deptID);
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
            if (status != null) {

                loadRequests(db.getLeaveRequestsByStatusAndDept(status, deptID));
            }
        } else if ("By Instructor".equals(mode)) {
            int idx = valueCombo.getSelectedIndex();
            if (idx >= 0 && instructors != null) {

                loadRequests(db.getLeaveRequestsByInstructorAndDept(
                        instructors.get(idx).getInstructorID(), deptID));
            }
        } else {
            loadRequests(db.getLeaveRequestsByDept(deptID));
        }
    }

    private void loadRequests(List<LeaveRequest> list) {
        currentList = list;
        tableModel.setRowCount(0);
        for (LeaveRequest lr : list) {
            tableModel.addRow(new Object[]{
                    lr.getLeaveReqID(),
                    lr.getInstructorName() != null ? lr.getInstructorName() : lr.getInstructID(),
                    lr.getLeaveType(),
                    lr.getStartDate(),
                    lr.getEndDate(),
                    lr.getStatus()
            });
        }
    }

    // ── Reason popup ───────────────────────────────────────────────────────────

    private void showReasonDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a leave request first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LeaveRequest lr = currentList.get(row);
        String reason = lr.getLeaveReason();
        boolean hasReason = reason != null && !reason.isBlank();

        // ── Dialog shell ───────────────────────────────────────────────────────
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Leave Reason  —  Request #" + lr.getLeaveReqID(),
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 340);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // ── Accent header ──────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIHelper.ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel headerTitle = new JLabel("Leave Reason");
        headerTitle.setFont(UIHelper.FONT_TITLE);
        headerTitle.setForeground(Color.WHITE);
        header.add(headerTitle, BorderLayout.WEST);

        // ── Meta grid (one field per row) ─────────────────────────────────────
        JPanel meta = new JPanel(new GridLayout(4, 2, 6, 4));
        meta.setBackground(UIHelper.BG);
        meta.setBorder(BorderFactory.createEmptyBorder(10, 16, 8, 16));

        meta.add(makeMetaLabel("Instructor:"));
        meta.add(makeMetaValue(lr.getInstructorName() != null
                ? lr.getInstructorName() : "ID " + lr.getInstructID()));
        meta.add(makeMetaLabel("Type / Status:"));
        meta.add(makeMetaValue(lr.getLeaveType() + "  ·  " + lr.getStatus()));
        meta.add(makeMetaLabel("Start Date:"));
        meta.add(makeMetaValue(lr.getStartDate().toString()));
        meta.add(makeMetaLabel("End Date:"));
        meta.add(makeMetaValue(lr.getEndDate().toString()));

        // ── Reason text area ──────────────────────────────────────────────────
        JTextArea textArea = new JTextArea(hasReason ? reason : "(No reason provided)");
        textArea.setFont(hasReason ? UIHelper.FONT_SUB
                : UIHelper.FONT_SUB.deriveFont(Font.ITALIC));
        textArea.setForeground(hasReason ? UIHelper.TEXT_DARK : UIHelper.TEXT_MID);
        textArea.setBackground(UIHelper.SURFACE);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JScrollPane textScroll = new JScrollPane(textArea);
        textScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER));

        // ── Footer Actions ──────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        footer.setBackground(UIHelper.BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER));

        if (currentUser instanceof DeptHead && lr.getStatus().equals("Pending")) {
            // DEPT HEAD: Can Approve or Reject
            JButton approveBtn = UIHelper.button("Approve");
            JButton rejectBtn = UIHelper.secondaryButton("Reject");

            approveBtn.addActionListener(e -> {
                if (db.updateLeaveStatus(
                        lr.getInstructID(),
                        lr.getLeaveReqID(),
                        "Approved",
                        currentUser.getUserID())) {
                    dialog.dispose();
                    applyFilter();
                }
            });

            rejectBtn.addActionListener(e -> {
                if (db.updateLeaveStatus(
                        lr.getInstructID(),
                        lr.getLeaveReqID(),
                        "Rejected",
                        currentUser.getUserID())) {
                    dialog.dispose();
                    applyFilter();
                }
            });

            footer.add(rejectBtn);
            footer.add(approveBtn);

        } else if (currentUser instanceof Secretary && lr.getStatus().equals("Approved")) {
            // SECRETARY: Can Sync to Attendance (Update absences filed in advance)
            JButton syncBtn = UIHelper.button("Sync to Attendance");

            syncBtn.addActionListener(e -> {
                db.syncLeaveToAttendance(lr);
                JOptionPane.showMessageDialog(dialog, "Absences updated in advance for this leave.");
                dialog.dispose();
            });

            footer.add(syncBtn);
        }

        JButton closeBtn = UIHelper.secondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        footer.add(closeBtn);

        // ── Assemble ──────────────────────────────────────────────────────────
        JPanel centre = new JPanel(new BorderLayout());
        centre.add(meta, BorderLayout.NORTH);
        centre.add(textScroll, BorderLayout.CENTER);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(centre, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JLabel makeMetaLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIHelper.FONT_LABEL);
        l.setForeground(UIHelper.TEXT_MID);
        return l;
    }

    private JLabel makeMetaValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIHelper.FONT_SUB);
        l.setForeground(UIHelper.TEXT_DARK);
        return l;
    }
}