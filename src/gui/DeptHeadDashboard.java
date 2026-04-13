package gui;

import app.AppController;

public class DeptHeadDashboard extends DashboardBase {
    public DeptHeadDashboard(AppController c) {
        super(c);
        buildDashboard("Department Head", c.getCurrentUser().getName(), new String[][]{
                {"View Leave Requests",      "leave"},
                {"Update Leave Request",     "updateLeave"},
                {"View Attendance Records",  "attendance"},
                {"View Class Schedules",     "schedules"},
                {"Create Class Schedule",    "createSchedule"},
        });
    }
    @Override protected void handleAction(String cmd) {
        if ("leave".equals(cmd))           controller.showLeaveRequests();
        if ("updateLeave".equals(cmd))     controller.showUpdateLeaveRequest();
        if ("attendance".equals(cmd))      controller.showAttendanceInstructorList();
        if ("schedules".equals(cmd))       controller.showClassSchedules();
        if ("createSchedule".equals(cmd))  controller.showCreateSchedule();
    }
}