package gui;

import app.AppController;
import app.DataAccess;
import ref.Attendance;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin panel that shows attendance records for ALL instructors in one flat table.
 */
public class AttendanceRecordsPanel extends BasePanel {

    private final DataAccess db;

    public AttendanceRecordsPanel(AppController controller, DataAccess db) {
        super(controller);
        this.db = db;
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.topBar("Attendance Records", "All Users"), BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Class Code", "Date", "Status", "Remarks",
                        "Assigned Instr. ID", "Actual Instr. ID", "Leave Req. ID", "Checked By"},
                0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Attendance> list = db.getAttendanceRecords();
        for (Attendance a : list) {
            model.addRow(new Object[]{
                    a.getAttendanceID(),
                    a.getClassCode(),
                    a.getStartDate(),
                    a.getInstructorStatus(),
                    a.getRemarks(),
                    a.getAssignedInstructID(),
                    a.getActualInstructID(),
                    a.getLeaveRequestID(),
                    a.getCheckedBy()
            });
        }

        JTable table = UIHelper.makeTable(model);

        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setBackground(UIHelper.BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));

        String hint = list.isEmpty() ? "No attendance records found." : "Total records: " + list.size();
        body.add(UIHelper.sub(hint), BorderLayout.NORTH);
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
        add(bottomBar(), BorderLayout.SOUTH);
    }
}