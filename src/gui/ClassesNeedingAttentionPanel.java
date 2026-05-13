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
 * Shows class schedules that are missing an instructor or an assigned checker.
 *
 * Visible to: Admin, DeptHead, Secretary
 * Editing follows the same role rules as ClassScheduleDetailDialog:
 *   - Admin      → checker only
 *   - DeptHead   → instructor + checker (dept-scoped)
 *   - Secretary  → instructor + checker (dept-scoped)
 */
public class ClassesNeedingAttentionPanel extends BasePanel {

    private final DataAccess db;
    private final SystemUser currentUser;
    private List<ClassSchedule> schedules;

    public ClassesNeedingAttentionPanel(AppController controller, DataAccess db, SystemUser currentUser) {
        super(controller);
        this.db   = db;
        this.currentUser = currentUser;
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.topBar("Classes Needing Attention", "Missing instructor or checker"), BorderLayout.NORTH);

        // Load only the "incomplete" schedules scoped to role
        int deptID = getDeptID();
        schedules = (deptID == -1)
                ? db.getSchedulesNeedingAttention()
                : db.getSchedulesNeedingAttentionByDept(deptID);

        String[] cols = {"Class Code", "Course No.", "Days", "Start Time", "Issue"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        for (ClassSchedule s : schedules) {
            String issue = buildIssueText(s);
            model.addRow(new Object[]{
                    s.getClassCode(),
                    s.getCourseNo(),
                    s.getDays(),
                    s.getStartTime(),
                    issue
            });
        }

        JTable table = UIHelper.makeTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);

        // Click row → open detail/edit dialog
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row < 0) return;
                ClassSchedule selected = schedules.get(row);
                Window parent = SwingUtilities.getWindowAncestor(ClassesNeedingAttentionPanel.this);
                new ClassScheduleDetailDialog(
                        parent instanceof Frame ? (Frame) parent : null,
                        db, currentUser, selected,
                        ClassesNeedingAttentionPanel.this::refresh
                ).setVisible(true);
            }
        });

        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setBackground(UIHelper.BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));

        String subtitle = schedules.isEmpty()
                ? "All classes are fully assigned."
                : "Click a row to assign missing details.  Total: " + schedules.size();
        body.add(UIHelper.sub(subtitle), BorderLayout.NORTH);
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
        add(bottomBar(), BorderLayout.SOUTH);
    }

    private String buildIssueText(ClassSchedule s) {
        boolean noInstructor = s.getInstructID() == null;
        boolean noChecker    = s.getAssignedChecker() == null;
        if (noInstructor && noChecker) return "No instructor, No checker";
        if (noInstructor) return "No instructor assigned";
        return "No checker assigned";
    }

    private int getDeptID() {
        String role = currentUser.getRole();
        if (role != null && (role.equalsIgnoreCase("Secretary") || role.equalsIgnoreCase("DeptHead"))) {
            Integer id = currentUser.getDepartmentID();

            return (id != null) ? id : -1;
        }
        return -1;
    }

    private void refresh() {
        controller.showClassesNeedingAttention();
    }
}