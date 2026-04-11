package gui;

import app.AppController;
import app.DataAccess;
import ref.Attendance;
import ref.Instructor;
import ref.LeaveRequest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AttendanceDetailPanel extends BasePanel {

    private final DataAccess db;
    private final Instructor instructor;

    public AttendanceDetailPanel(AppController controller, DataAccess db, Instructor instructor) {
        super(controller);
        this.db = db;
        this.instructor = instructor;
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.topBar("Attendance Detail",
                instructor.getName() + "  ·  " + instructor.getDepartment()), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(UIHelper.BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));

        // ── Stats ──────────────────────────────────────────────────────────────
        int present  = db.getPresentCount(instructor.getInstructorID());
        int absent   = db.getAbsenceCount(instructor.getInstructorID());
        List<LeaveRequest> leaves = db.getLeaveRequestsByInstructor(instructor.getInstructorID());

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow.setOpaque(false);
        statsRow.add(UIHelper.statChip("Classes Present",  String.valueOf(present)));
        statsRow.add(UIHelper.statChip("Absences",         String.valueOf(absent)));
        statsRow.add(UIHelper.statChip("Leave Requests",   String.valueOf(leaves.size())));

        // ── Leave requests table ───────────────────────────────────────────────
        JLabel leaveTitle = new JLabel("Leave Requests  (most recent first)");
        leaveTitle.setFont(UIHelper.FONT_LABEL);
        leaveTitle.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));

        String[] lCols = {"Req ID", "Type", "Start Date", "End Date", "Status"};
        DefaultTableModel lModel = new DefaultTableModel(lCols, 0);
        for (LeaveRequest lr : leaves) {
            lModel.addRow(new Object[]{
                    lr.getLeaveReqID(), lr.getLeaveType(),
                    lr.getStartDate(), lr.getEndDate(), lr.getStatus()
            });
        }
        JTable leaveTable = UIHelper.makeTable(lModel);
        leaveTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        leaveTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        leaveTable.getColumnModel().getColumn(4).setPreferredWidth(110);

        JPanel leaveSection = new JPanel(new BorderLayout());
        leaveSection.setOpaque(false);
        leaveSection.add(leaveTitle, BorderLayout.NORTH);
        leaveSection.add(UIHelper.scroll(leaveTable), BorderLayout.CENTER);

        // ── Attendance records table ───────────────────────────────────────────
        JLabel attTitle = new JLabel("Attendance Records  (most recent first)");
        attTitle.setFont(UIHelper.FONT_LABEL);
        attTitle.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));

        List<Attendance> attList = db.getAttendanceByInstructor(instructor.getInstructorID());
        String[] aCols = {"Class Code", "Date", "Status", "Substitute?"};
        DefaultTableModel aModel = new DefaultTableModel(aCols, 0);
        for (Attendance a : attList) {
            aModel.addRow(new Object[]{
                    a.getClassCode(), a.getDate(),
                    a.getInstructorStatus(), a.isSubstitute() ? "Yes" : "No"
            });
        }
        JTable attTable = UIHelper.makeTable(aModel);

        JPanel attSection = new JPanel(new BorderLayout());
        attSection.setOpaque(false);
        attSection.add(attTitle, BorderLayout.NORTH);
        attSection.add(UIHelper.scroll(attTable), BorderLayout.CENTER);

        // ── Layout ─────────────────────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leaveSection, attSection);
        split.setDividerLocation(220);
        split.setResizeWeight(0.45);
        split.setBorder(null);
        split.setOpaque(false);

        body.add(statsRow, BorderLayout.NORTH);
        body.add(split, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        // Back goes to instructor list, not dashboard
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setBackground(UIHelper.BG);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER));
        JButton back = UIHelper.secondaryButton("← Back");
        back.addActionListener(e -> controller.showAttendanceInstructorList());
        bar.add(back);
        add(bar, BorderLayout.SOUTH);
    }
}