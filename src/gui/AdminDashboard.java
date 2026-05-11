package gui;

import app.AppController;

public class AdminDashboard extends DashboardBase {

    public AdminDashboard(AppController controller) {

        super(controller);

        buildDashboard(
                "Admin",
                controller.getCurrentUser().getName(),
                new String[][]{
                        {"View All Accounts", "accounts"},
                        {"Create Account", "createAccount"},
                        {"View Attendance Records", "attendance"},
                        {"View Attendance by Instructor", "attendanceByInstructor"},
                        {"View Class Schedules", "schedules"},
                        {"Classes Needing Attention", "needsAttention"}
                }
        );
    }

    @Override
    protected void handleAction(String command) {

        if ("accounts".equals(command)) {
            controller.showAccountList();
        }

        if ("createAccount".equals(command)) {
            controller.showCreateAccount();
        }

        if ("attendance".equals(command)) {
            controller.showAttendanceRecords();
        }

        if ("attendanceByInstructor".equals(command)) {
            controller.showAttendanceInstructorList();
        }

        if ("schedules".equals(command)) {
            controller.showClassSchedules();
        }

        if ("needsAttention".equals(command)) {
            controller.showClassesNeedingAttention();
        }
    }
}