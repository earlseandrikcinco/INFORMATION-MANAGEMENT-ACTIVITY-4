package gui;

import app.AppController;
import app.DataAccess;
import ref.Attendance;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Admin panel that shows attendance records for ALL instructors.
 * Features:
 *  - Displays names instead of raw IDs.
 *  - Double-click a row to view complete record details.
 */
public class AttendanceRecordsPanel extends BasePanel {

    private final DataAccess db;
    private JTable table;
    private DefaultTableModel model;
    private List<Attendance> attendanceList;

    public AttendanceRecordsPanel(AppController controller, DataAccess db) {
        super(controller);
        this.db = db;
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.topBar("Attendance Records", "System-wide Logs"), BorderLayout.NORTH);

        // ── Table Setup ──────────────────────────────────────────────────
        // We show Names here instead of IDs for better readability
        String[] columns = {
                "ID", "Class Code", "Date", "Status", "Instructor", "Checked By"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = UIHelper.makeTable(model);

        // Add double-click functionality
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    showDetailsDialog(attendanceList.get(table.getSelectedRow()));
                }
            }
        });

        // ── Load Data ─────────────────────────────────────────────────────
        refreshData();

        // ── Layout ────────────────────────────────────────────────────────
        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setBackground(UIHelper.BG);
        body.setBorder(new EmptyBorder(12, 18, 12, 18));

        String stats = attendanceList.isEmpty() ? "No records found." : "Total: " + attendanceList.size() + " records";
        JLabel statsLbl = UIHelper.sub(stats + " · Double-click row for details");

        body.add(statsLbl, BorderLayout.NORTH);
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
        add(bottomBar(), BorderLayout.SOUTH);
    }

    private void refreshData() {
        model.setRowCount(0);
        attendanceList = db.getAttendanceRecords();

        for (Attendance a : attendanceList) {
            model.addRow(new Object[]{
                    a.getAttendanceID(),
                    a.getClassCode(),
                    a.getStartDate(),
                    a.getInstructorStatus(),
                    // Use Name if available, fallback to ID if join failed
                    a.getInstructorName() != null ? a.getInstructorName() : "ID: " + a.getAssignedInstructID(),
                    a.getCheckerName() != null ? a.getCheckerName() : "ID: " + a.getCheckedBy()
            });
        }
    }

    /**
     * Shows a detailed view of the specific attendance record.
     */
    private void showDetailsDialog(Attendance a) {
        String details = String.format("""
            <html>
            <body style='width: 300px; font-family: sans-serif; padding: 5px;'>
                <h2 style='color: #2c3e50; margin-bottom: 0;'>Attendance Details</h2>
                <hr>
                <table border='0' cellpadding='4'>
                    <tr><td><b>Attendance ID:</b></td><td>%d</td></tr>
                    <tr><td><b>Class Code:</b></td><td>%s</td></tr>
                    <tr><td><b>Date:</b></td><td>%s</td></tr>
                    <tr><td><br></td></tr>
                    <tr><td><b>Assigned to:</b></td><td>%s</td></tr>
                    <tr><td><b>Actual Present:</b></td><td>%s</td></tr>
                    <tr><td><b>Checked By:</b></td><td>%s</td></tr>
                    <tr><td><br></td></tr>
                    <tr><td><b>Status:</b></td><td><b style='color: blue;'>%s</b></td></tr>
                    <tr><td><b>Remarks:</b></td><td><i>%s</i></td></tr>
                </table>
            </body>
            </html>
            """,
                a.getAttendanceID(),
                a.getClassCode(),
                a.getStartDate(),
                (a.getInstructorName() != null ? a.getInstructorName() : "ID " + a.getAssignedInstructID()),
                (a.getActualInstructorName() != null ? a.getActualInstructorName() : "None (Absent/No Sub)"),
                (a.getCheckerName() != null ? a.getCheckerName() : "ID " + a.getCheckedBy()),
                a.getInstructorStatus(),
                (a.getRemarks() != null && !a.getRemarks().isEmpty() ? a.getRemarks() : "None")
        );

        JOptionPane.showMessageDialog(
                this,
                details,
                "Record #" + a.getAttendanceID(),
                JOptionPane.PLAIN_MESSAGE
        );
    }
}