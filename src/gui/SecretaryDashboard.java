package gui;

import app.AppController;

public class SecretaryDashboard extends DashboardBase {
    public SecretaryDashboard(AppController c) {
        super(c);
        buildDashboard("Secretary", c.getCurrentUser().getName(), new String[][]{
                {"View Leave Requests",      "leave"},
                {"View Attendance Records",  "attendance"},
        });
    }
    @Override protected void handleAction(String cmd) {
        if ("leave".equals(cmd))      controller.showLeaveRequests();
        if ("attendance".equals(cmd)) controller.showAttendanceInstructorList();
    }
}