package gui;

import app.AppController;

public class DeptHeadDashboard extends DashboardBase {
    public DeptHeadDashboard(AppController c) {
        super(c);
        buildDashboard("Department Head", c.getCurrentUser().getName(), new String[][]{
                {"View Leave Requests",      "leave"},
                {"View Attendance Records",  "attendance"},
                {"View Class Schedules",     "schedules"},
        });
    }
    @Override protected void handleAction(String cmd) {
        if ("leave".equals(cmd))      controller.showLeaveRequests();
        if ("attendance".equals(cmd)) controller.showAttendanceInstructorList();
        if ("schedules".equals(cmd))  controller.showClassSchedules();
    }
}