package gui;

import app.AppController;

public class DeptHeadDashboard extends DashboardBase {

    public DeptHeadDashboard(AppController controller) {

        super(controller);

        buildDashboard(
                "Department Head",
                controller.getCurrentUser().getName(),
                new String[][]{
                        {"View Leave Requests", "leave"},
                        {"Update Leave Request", "updateLeave"},
                        {"View Attendance Records", "attendance"},
                        {"View Class Schedules", "schedules"},
                        {"Create Class Schedule", "createSchedule"},
                        {"Classes Needing Attention", "needsAttention"}
                }
        );
    }

    @Override
    protected void handleAction(String command) {

        if ("leave".equals(command)) {
            controller.showLeaveRequests();
        }

        if ("updateLeave".equals(command)) {
            controller.showUpdateLeaveRequest();
        }

        if ("attendance".equals(command)) {
            controller.showAttendanceInstructorList();
        }

        if ("schedules".equals(command)) {
            controller.showClassSchedules();
        }

        if ("createSchedule".equals(command)) {
            controller.showCreateSchedule();
        }

        if ("needsAttention".equals(command)) {
            controller.showClassesNeedingAttention();
        }
    }
}