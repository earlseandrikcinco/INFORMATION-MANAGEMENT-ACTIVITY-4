package gui;

import app.AppController;

public class CheckerDashboard extends DashboardBase {
    public CheckerDashboard(AppController c) {
        super(c);
        buildDashboard("Checker", c.getCurrentUser().getName(), new String[][]{
                {"My Class Schedules",      "mySchedules"},
                {"Update Attendance",       "updateAttendance"},
                {"View Attendance Records", "attendance"},
        });
    }
    @Override protected void handleAction(String cmd) {
        if ("mySchedules".equals(cmd))      controller.showClassSchedulesByChecker();
        if ("updateAttendance".equals(cmd)) controller.showUpdateAttendance();
        if ("attendance".equals(cmd))       controller.showAttendanceInstructorList();
    }
}