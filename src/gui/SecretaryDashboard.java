package gui;

import app.AppController;

public class SecretaryDashboard extends DashboardBase {

    public SecretaryDashboard(AppController controller) {

        super(controller);

        buildDashboard(
                "Secretary",
                controller.getCurrentUser().getName(),
                new String[][]{
                        {"View Leave Requests", "leave"},
                        {"View Attendance Records", "attendance"},
                        {"View Class Schedules", "schedules"},
                        {"Classes Needing Attention", "needsAttention"}
                }
        );
    }

    @Override
    protected void handleAction(String command) {

        if ("leave".equals(command)) {
            controller.showLeaveRequests();
        }

        if ("attendance".equals(command)) {
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