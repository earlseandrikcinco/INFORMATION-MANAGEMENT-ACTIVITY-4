package gui;

import app.AppController;

public class AdminDashboard extends DashboardBase {
    public AdminDashboard(AppController c) {
        super(c);
        buildDashboard("Admin", c.getCurrentUser().getName(), new String[][]{
                {"View All Accounts",       "accounts"},
                {"Create New User",         "create_user"}, // Added this line
                {"View Attendance Records", "attendance"},
        });
    }

    @Override
    protected void handleAction(String cmd) {
        if ("accounts".equals(cmd)) {
            controller.showAccountList();
        } else if ("create_user".equals(cmd)) {
            controller.showCreateUserForm();
        } else if ("attendance".equals(cmd)) {
            controller.showAttendanceInstructorList();
        }
    }
}