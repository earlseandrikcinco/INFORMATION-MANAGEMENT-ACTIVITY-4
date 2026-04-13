package gui;

import app.AppController;

public class AdminDashboard extends DashboardBase {
    public AdminDashboard(AppController c) {
        super(c);
        buildDashboard("Admin", c.getCurrentUser().getName(), new String[][]{
                {"View All Accounts",       "accounts"},
                {"Create Account",          "createAccount"},
                {"View Attendance Records", "attendance"},
        });
    }
    @Override protected void handleAction(String cmd) {
        if ("accounts".equals(cmd))      controller.showAccountList();
        if ("createAccount".equals(cmd)) controller.showCreateAccount();
        if ("attendance".equals(cmd))    controller.showAttendanceInstructorList();
    }
}