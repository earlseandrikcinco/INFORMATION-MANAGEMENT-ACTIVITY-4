package gui;

import app.AppController;
import app.DataAccess;
import ref.ClassSchedule;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClassSchedulePanel extends BasePanel {

    private final DataAccess db;

    public ClassSchedulePanel(AppController controller, DataAccess db) {
        super(controller);
        this.db = db;
        buildUI();
    }

    private void buildUI() {
        add(UIHelper.topBar("Class Schedules", ""), BorderLayout.NORTH);

        List<ClassSchedule> schedules = db.getAllClassSchedules();

        String[] cols = {"Class Code", "Course No.", "Days", "Start", "End", "Instructor", "Room"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (ClassSchedule s : schedules) {
            model.addRow(new Object[]{
                    s.getClassCode(),
                    s.getCourseNo(),
                    s.getDays(),
                    s.getStartTime(),
                    s.getEndTime(),
                    s.getInstructorName() != null ? s.getInstructorName() : "—",
                    s.getRoomID()          != null ? s.getRoomID()           : "—"
            });
        }

        JTable table = UIHelper.makeTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(75);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(170);
        table.getColumnModel().getColumn(6).setPreferredWidth(120);

        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setBackground(UIHelper.BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 10, 18));
        body.add(UIHelper.sub("Total schedules: " + schedules.size()), BorderLayout.NORTH);
        body.add(UIHelper.scroll(table), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
        add(bottomBar(), BorderLayout.SOUTH);
    }
}