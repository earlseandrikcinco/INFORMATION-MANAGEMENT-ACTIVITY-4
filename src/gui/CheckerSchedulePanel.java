package gui;

import app.AppController;
import app.DataAccess;
import ref.ClassSchedule;
import ref.SystemUser;

import javax.swing.*;
        import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
        import java.util.List;

/**
 * Read-only panel that shows only the class schedules assigned to the
 * currently logged-in Checker.  Only visible when role == CHECKER.
 */
public class CheckerSchedulePanel extends BasePanel {

    private final DataAccess  db;
    private final SystemUser  currentUser;

    private DefaultTableModel tableModel;
    private JTable            table;

    public CheckerSchedulePanel(AppController controller, DataAccess db, SystemUser currentUser) {
        super(controller);
        this.db          = db;
        this.currentUser = currentUser;
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.topBar("My Assigned Schedules", "Checker · " + currentUser.getName()),
                BorderLayout.NORTH);

        // ── table ──────────────────────────────────────────────────────────
        String[] cols = {"Class Code", "Course No.", "Days", "Start Time", "End Time", "Instructor"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.makeTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(180);

        JPanel body = new JPanel(new BorderLayout(0, 4));
        body.setBackground(UIHelper.BG);
        body.setBorder(new EmptyBorder(10, 18, 10, 18));

        // Refresh toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);
        JButton refreshBtn = UIHelper.secondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadData());
        toolbar.add(refreshBtn);

        body.add(toolbar,            BorderLayout.NORTH);
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(body,        BorderLayout.CENTER);
        add(bottomBar(), BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<ClassSchedule> schedules = db.getSchedulesByCheckerID(currentUser.getUserID());
        for (ClassSchedule cs : schedules) {
            tableModel.addRow(new Object[]{
                    cs.getClassCode(),
                    cs.getCourseNo(),
                    cs.getDays(),
                    cs.getStartTime(),
                    cs.getEndTime(),
                    cs.getInstructorName() != null ? cs.getInstructorName() : "—"
            });
        }
        if (schedules.isEmpty()) {
            tableModel.addRow(new Object[]{"No schedules assigned.", "", "", "", "", ""});
        }
    }
}