package gui;

import app.AppController;
import app.DataAccess;
import ref.DeptHead;
import ref.LeaveRequest;
import ref.SystemUser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * DeptHead panel for approving or rejecting pending leave requests
 * from instructors within their own department.
 */
public class UpdateLeaveRequestPanel extends BasePanel {

    private final DataAccess db;
    private final SystemUser currentUser;

    private DefaultTableModel tableModel;
    private JTable table;
    private List<LeaveRequest> currentList = new ArrayList<>();

    public UpdateLeaveRequestPanel(AppController controller, DataAccess db, SystemUser currentUser) {
        super(controller);
        this.db = db;
        this.currentUser = currentUser;
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.topBar("Update Leave Request", "Department Head — Pending requests"), BorderLayout.NORTH);

        String[] cols = {"Req No", "Instructor", "Leave Type", "Start Date", "End Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0);
        table = UIHelper.makeTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showActionDialog();
            }
        });

        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setBackground(UIHelper.BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));

        JLabel hint = UIHelper.sub("Select a request and click Approve / Reject, or double-click a row.");
        body.add(hint, BorderLayout.NORTH);
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);

        JButton approveBtn = UIHelper.button("✓ Approve");
        approveBtn.setBackground(new Color(40, 140, 70));
        approveBtn.addActionListener(e -> resolveSelected("Approved"));

        JButton rejectBtn = UIHelper.button("✗ Reject");
        rejectBtn.setBackground(new Color(190, 50, 50));
        rejectBtn.addActionListener(e -> resolveSelected("Rejected"));

        add(bottomBar(approveBtn, rejectBtn), BorderLayout.SOUTH);

        loadRequests();
    }


    private void loadRequests() {
        int deptID = getDeptID();
        List<LeaveRequest> all = db.getLeaveRequestsByStatusAndDept("Pending", deptID);
        currentList = all;
        tableModel.setRowCount(0);
        for (LeaveRequest lr : all) {
            tableModel.addRow(new Object[]{
                    lr.getLeaveReqID(),
                    lr.getInstructorName() != null ? lr.getInstructorName() : "ID " + lr.getInstructID(),
                    lr.getLeaveType(),
                    lr.getStartDate(),
                    lr.getEndDate(),
                    lr.getStatus()
            });
        }
    }

    private int getDeptID() {
        if (currentUser instanceof DeptHead) return ((DeptHead) currentUser).getDepartmentID();
        return -1;
    }

    private void resolveSelected(String newStatus) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a leave request first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LeaveRequest lr = currentList.get(row);
        String label = "Approved".equals(newStatus) ? "approve" : "reject";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + label + " this leave request?\n\n"
                        + "Instructor: " + (lr.getInstructorName() != null ? lr.getInstructorName() : lr.getInstructID()) + "\n"
                        + "Type: " + lr.getLeaveType() + "\n"
                        + lr.getStartDate() + " → " + lr.getEndDate(),
                "Confirm " + newStatus,
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = db.resolveLeaveRequest(
                lr.getInstructID(),
                lr.getLeaveReqID(),
                newStatus,
                currentUser.getUserID()
        );


        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Leave request " + newStatus.toLowerCase() + " successfully.",
                    "Done", JOptionPane.INFORMATION_MESSAGE);
            loadRequests();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to update the leave request. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Full detail popup with Approve / Reject buttons embedded. */
    private void showActionDialog() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        LeaveRequest lr = currentList.get(row);

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Leave Request  —  #" + lr.getLeaveReqID(),
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 340);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIHelper.ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("Leave Request Detail");
        title.setFont(UIHelper.FONT_TITLE);
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel grid = new JPanel(new GridLayout(5, 2, 6, 6));
        grid.setBackground(UIHelper.BG);
        grid.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));
        grid.add(metaLabel("Instructor:"));
        grid.add(metaValue(lr.getInstructorName() != null ? lr.getInstructorName() : "ID " + lr.getInstructID()));
        grid.add(metaLabel("Leave Type:"));
        grid.add(metaValue(lr.getLeaveType()));
        grid.add(metaLabel("Start Date:"));
        grid.add(metaValue(lr.getStartDate().toString()));
        grid.add(metaLabel("End Date:"));
        grid.add(metaValue(lr.getEndDate().toString()));
        grid.add(metaLabel("Current Status:"));
        grid.add(metaValue(lr.getStatus()));

        String reason = lr.getLeaveReason();
        boolean hasReason = reason != null && !reason.isBlank();
        JTextArea reasonArea = new JTextArea(hasReason ? reason : "(No reason provided)");
        reasonArea.setFont(hasReason ? UIHelper.FONT_SUB : UIHelper.FONT_SUB.deriveFont(Font.ITALIC));
        reasonArea.setForeground(hasReason ? UIHelper.TEXT_DARK : UIHelper.TEXT_MID);
        reasonArea.setBackground(UIHelper.SURFACE);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setEditable(false);
        reasonArea.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER));

        JPanel centre = new JPanel(new BorderLayout());
        centre.add(grid, BorderLayout.NORTH);
        centre.add(reasonScroll, BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        foot.setBackground(UIHelper.BG);
        foot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER));

        JButton closeBtn = UIHelper.secondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());

        JButton rejectBtn = UIHelper.button("✗ Reject");
        rejectBtn.setBackground(new Color(190, 50, 50));
        rejectBtn.setPreferredSize(new Dimension(110, 32));
        rejectBtn.addActionListener(e -> {
            dialog.dispose();
            resolveSelected("Rejected");
        });

        JButton approveBtn = UIHelper.button("✓ Approve");
        approveBtn.setBackground(new Color(40, 140, 70));
        approveBtn.setPreferredSize(new Dimension(110, 32));
        approveBtn.addActionListener(e -> {
            dialog.dispose();
            resolveSelected("Approved");
        });

        foot.add(closeBtn);
        foot.add(rejectBtn);
        foot.add(approveBtn);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(centre, BorderLayout.CENTER);
        dialog.add(foot, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JLabel metaLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIHelper.FONT_LABEL);
        l.setForeground(UIHelper.TEXT_MID);
        return l;
    }

    private JLabel metaValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIHelper.FONT_SUB);
        l.setForeground(UIHelper.TEXT_DARK);
        return l;
    }
}