package gui;

import app.AppController;

public class AdminDashboard extends DashboardBase {
    public AdminDashboard(AppController c) {
        super(c);
        buildDashboard("Admin", c.getCurrentUser().getName(), new String[][]{
                {"View All Accounts",       "accounts"},
                {"View Attendance Records", "attendance"},
        });
    }
    @Override protected void handleAction(String cmd) {
        if ("accounts".equals(cmd))   controller.showAccountList();
        if ("attendance".equals(cmd)) controller.showAttendanceInstructorList();
    }
}