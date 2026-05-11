package gui;

import app.AppController;
import app.DataAccess;
import ref.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Shows a summary table of class schedules (classCode, courseNo, days, startTime).
 * Clicking a row opens ClassScheduleDetailDialog for full details and optional editing.
 *
 * Visible to: Admin, DeptHead, Secretary
 * Edit rights: DeptHead and Secretary → instructor + checker; Admin → checker only
 */
public class ClassSchedulePanel extends BasePanel {

    private final DataAccess db;
    private final SystemUser user;
    private final int deptID;           // -1 = Admin (sees all)
    private List<ClassSchedule> schedules;

    public ClassSchedulePanel(AppController controller, DataAccess db, SystemUser user) {
        super(controller);
        this.db   = db;
        this.user = user;

        if (user instanceof DeptHead dh) {
            deptID = dh.getDepartmentID();
        } else if (user instanceof Secretary sec) {
            deptID = sec.getDepartmentID();
        } else {
            deptID = -1;   // Admin sees everything
        }

        buildUI();
    }

    private void buildUI() {
        add(UIHelper.topBar("Class Schedules", ""), BorderLayout.NORTH);

        // Load data
        schedules = (deptID == -1)
                ? db.getAllClassSchedules()
                : db.getAllClassSchedulesByDept(deptID);

        // Summary columns only
        String[] cols = {"Class Code", "Course No.", "Days", "Start Time"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (ClassSchedule s : schedules) {
            model.addRow(new Object[]{
                    s.getClassCode(),
                    s.getCourseNo(),
                    s.getDays(),
                    s.getStartTime()
            });
        }

        JTable table = UIHelper.makeTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);

        // Click row → open detail dialog
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row < 0) return;
                ClassSchedule selected = schedules.get(row);
                Window parent = SwingUtilities.getWindowAncestor(ClassSchedulePanel.this);
                new ClassScheduleDetailDialog(
                        parent instanceof Frame ? (Frame) parent : null,
                        db, user, selected,
                        ClassSchedulePanel.this::refresh
                ).setVisible(true);
            }
        });

        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setBackground(UIHelper.BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));
        body.add(UIHelper.sub("Click a row to view full details.  Total: " + schedules.size()), BorderLayout.NORTH);
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
        add(bottomBar(), BorderLayout.SOUTH);
    }

    /** Reloads the panel in place after an edit. */
    private void refresh() {
        controller.showClassSchedules();
    }
}