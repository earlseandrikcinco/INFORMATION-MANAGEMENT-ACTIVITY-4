package app;

import gui.*;
import ref.*;

import javax.swing.*;

public class AppController {

    private MainFrame frame;
    private DataAccess db;

    private SystemUser currentUser;

    public AppController() {
        db = new DataAccess();
    }

    public void start() {
        frame = new MainFrame(this);
        frame.setVisible(true);
        frame.showPanel(new LoginPanel(this));
    }

    public SystemUser getCurrentUser() { return currentUser; }

    public void login(String username, String password) {
        SystemUser user = db.getUser(username);
        if (user == null) return;
        if (!user.getPassword().equals(password)) return;
        currentUser = user;
        showDashboard();
    }

    public void logout() {
        currentUser = null;
        frame.showPanel(new LoginPanel(this));
    }

    public void showDashboard() {
        if (currentUser == null) {
            frame.showPanel(new LoginPanel(this));
            return;
        }
        switch (currentUser.getRole().toUpperCase()) {
            case "ADMIN"     -> frame.showPanel(new AdminDashboard(this));
            case "SECRETARY" -> frame.showPanel(new SecretaryDashboard(this));
            case "DEPTHEAD"  -> frame.showPanel(new DeptHeadDashboard(this));
            case "CHECKER"   -> frame.showPanel(new CheckerDashboard(this));
            default          -> frame.showError("Unknown role: " + currentUser.getRole());
        }
    }

    public void showAccountList() {
        frame.showPanel(new AccountListPanel(this, db));
    }

    public void showCreateAccount() {
        frame.showPanel(new CreateAccountPanel(this, db, currentUser));
    }

    public void showAttendanceInstructorList() {
        frame.showPanel(new AttendanceInstructorListPanel(this, db, currentUser));
    }

    public void showAttendanceDetail(Instructor instructor) {
        frame.showPanel(new AttendanceDetailPanel(this, db, instructor));
    }

    public void showLeaveRequests() {
        String role = currentUser.getRole();
        if (!role.equalsIgnoreCase("Secretary") && !role.equalsIgnoreCase("DeptHead")) {
            frame.showError("Insufficient privileges.");
            return;
        }
        frame.showPanel(new LeaveRequestPanel(this, db, currentUser));
    }
    public void showAttendanceRecords() {
        frame.showPanel(new AttendanceRecordsPanel(this, db));
    }

    public void showUpdateLeaveRequest() {
        frame.showPanel(new UpdateLeaveRequestPanel(this, db, currentUser));
    }

    public void showClassSchedules() {
        frame.showPanel(new ClassSchedulePanel(this, db, currentUser));
    }

    // TODO Create a checker bound class schedules
    public void showClassSchedulesByChecker() {
        if ("Checker".equalsIgnoreCase(currentUser.getRole())) {
            return; // do nothing
        }

        frame.showPanel(new ClassSchedulePanel(this, db, currentUser));
    }

    public void showCreateSchedule() {
        frame.showPanel(new CreateSchedulePanel(this, db, currentUser));
    }

    public void showClassesNeedingAttention() {
        frame.showPanel(new ClassesNeedingAttentionPanel(this, db, currentUser));
    }

    public void showAffectedClasses(LeaveRequest lr) {
        frame.showPanel(new AffectedClassesPanel(this, db, lr, currentUser));
    }

    public void showUpdateAttendance() {
        frame.showPanel(new UpdateAttendancePanel(this, db, currentUser));
    }

    public void showCheckerDetails() {
        if (!currentUser.getRole().equalsIgnoreCase("Admin")) {
            frame.showError("Insufficient privileges.");
            return;
        }
        frame.showPanel(new CheckerDetailsPanel(this, db));
    }
}