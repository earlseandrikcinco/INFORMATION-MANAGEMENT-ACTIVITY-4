package gui;

import app.AppController;

public class CheckerDashboard extends DashboardBase {
    public CheckerDashboard(AppController c) {
        super(c);
        buildDashboard("Checker", c.getCurrentUser().getName(), new String[][]{
                {"View Class Schedules",     "schedules"},
                {"View Attendance Records",  "attendance"},
        });
    }
    @Override protected void handleAction(String cmd) {
        if ("schedules".equals(cmd))  controller.showClassSchedules();
        if ("attendance".equals(cmd)) controller.showAttendanceInstructorList();
    }
}