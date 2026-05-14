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

    private JPanel statsRow;
    private DefaultTableModel lModel;
    private DefaultTableModel aModel;
    private JTable attTable; // Moved to class level

    public AttendanceDetailPanel(AppController controller, DataAccess db, Instructor instructor) {
        super(controller);
        this.db = db;
        this.instructor = instructor;
        buildUI();
        refreshData();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        add(UIHelper.topBar("Instructor Attendance Overview",
                instructor.getName() + "  ·  ID: " + instructor.getInstructorID()), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(UIHelper.BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));

        statsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow.setOpaque(false);

        // --- Leave Section ---
        JLabel leaveTitle = new JLabel("Leave History");
        leaveTitle.setFont(UIHelper.FONT_LABEL);
        String[] lCols = {"Req ID", "Type", "Start Date", "End Date", "Status"};
        lModel = new DefaultTableModel(lCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable leaveTable = UIHelper.makeTable(lModel);

        JPanel leaveSection = new JPanel(new BorderLayout());
        leaveSection.setOpaque(false);
        leaveSection.add(leaveTitle, BorderLayout.NORTH);
        leaveSection.add(UIHelper.scroll(leaveTable), BorderLayout.CENTER);

        // --- Attendance Section ---
        JLabel attTitle = new JLabel("Detailed Class Attendance");
        attTitle.setFont(UIHelper.FONT_LABEL);

        // Match the columns used in refreshData
        String[] aCols = {"Class Code", "Date", "Status", "Substitute", "Leave Linked"};
        aModel = new DefaultTableModel(aCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        attTable = UIHelper.makeTable(aModel);

        // Red/Green Color Renderer
        attTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (t.getValueAt(r, 2) != null) {
                    String status = t.getValueAt(r, 2).toString();
                    if (status.contains("Absent")) comp.setForeground(Color.RED);
                    else if (status.equals("Present")) comp.setForeground(new Color(0, 120, 0));
                    else comp.setForeground(Color.BLACK);
                }
                return comp;
            }
        });

        JPanel attSection = new JPanel(new BorderLayout());
        attSection.setOpaque(false);
        attSection.add(attTitle, BorderLayout.NORTH);
        attSection.add(UIHelper.scroll(attTable), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leaveSection, attSection);
        split.setDividerLocation(200);
        split.setOpaque(false);
        split.setBorder(null);

        body.add(statsRow, BorderLayout.NORTH);
        body.add(split, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setBackground(UIHelper.BG);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER));
        JButton back = UIHelper.secondaryButton("← Back to Instructor List");
        back.addActionListener(e -> controller.showAttendanceInstructorList());
        bar.add(back);
        add(bar, BorderLayout.SOUTH);
    }

    public void refreshData() {
        int instructorID = instructor.getInstructorID();

        // 1. Stats
        int present = db.getPresentCount(instructorID);
        int unexcused = db.getUnexcusedAbsenceCount(instructorID); // Use the new logic
        List<LeaveRequest> leaves = db.getLeaveRequestsByInstructor(instructorID);

        statsRow.removeAll();
        statsRow.add(UIHelper.statChip("Classes Present", String.valueOf(present)));
        statsRow.add(UIHelper.statChip("Unexcused Absences", String.valueOf(unexcused)));
        statsRow.add(UIHelper.statChip("Leave Requests", String.valueOf(leaves.size())));

        // 2. Leave Table
        lModel.setRowCount(0);
        for (LeaveRequest lr : leaves) {
            lModel.addRow(new Object[]{
                    lr.getLeaveRequestID(), lr.getLeaveType(),
                    lr.getStartDate(), lr.getEndDate(), lr.getStatus()
            });
        }

        // 3. Attendance Table (Fixed Logic)
        aModel.setRowCount(0);
        List<Attendance> attList = db.getAttendanceByInstructor(instructorID);
        for (Attendance a : attList) {
            String subInfo = "-";

            // Check if this specific session had a substitute
            if (a.getActualInstructID() != null && a.getActualInstructID() != instructorID) {
                subInfo = "Sub: " + db.getActualInstructorName(a.getActualInstructID());
            }

            aModel.addRow(new Object[]{
                    a.getClassCode(),
                    a.getStartDate(), // Ensure ref.Attendance has getDate() or getStartDate()
                    translateStatus(a.getInstructorStatus()),
                    subInfo,
                    (a.getLeaveRequestID() != null && a.getLeaveRequestID() > 0)
                            ? "Req #" + a.getLeaveRequestID() : "-"
            });
        }

        statsRow.revalidate();
        statsRow.repaint();
    }

    private String translateStatus(String status) {
        if (status == null) return "Pending";
        return switch (status.toUpperCase()) {
            case "P", "PRESENT" -> "Present";
            case "A", "ABSENT"  -> "Absent (Unexcused)";
            case "SL"           -> "Sick Leave (Excused)";
            case "OB"           -> "Official Business (Excused)";
            case "PL"           -> "Personal Leave (Excused)";
            case "SUBSTITUTED"  -> "Substituted";
            default             -> status;
        };
    }
}