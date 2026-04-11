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

    // -------------------------------------------------
    // START APPLICATION
    // -------------------------------------------------

    public void start() {
        frame = new MainFrame(this);
        frame.setVisible(true);

        frame.showPanel(new LoginPanel(this));
    }

    // -------------------------------------------------
    // USER SESSION
    // -------------------------------------------------

    public SystemUser getCurrentUser() {
        return currentUser;
    }

    public void login(String username, String password) {

        SystemUser user = db.getUser(username);

        if (user == null) {
            return;
        }

        if (!user.getPassword().equals(password)) {
            return;
        }

        currentUser = user;
        showDashboard();
    }

    public void logout() {
        currentUser = null;
        frame.showPanel(new LoginPanel(this));
    }

    // -------------------------------------------------
    // DASHBOARD ROUTING
    // -------------------------------------------------

    public void showDashboard() {

        if (currentUser == null) {
            frame.showPanel(new LoginPanel(this));
            return;
        }

        String role = currentUser.getRole();

        switch (role) {
            case "Admin":
                frame.showPanel(new AdminDashboard(this));
                break;

            case "Secretary":
                frame.showPanel(new SecretaryDashboard(this));
                break;

            case "DeptHead":
                frame.showPanel(new DeptHeadDashboard(this));
                break;

            case "Checker":
                frame.showPanel(new CheckerDashboard(this));
                break;

            default:
                frame.showError("Unknown role: " + role);
        }
    }

    // -------------------------------------------------
    // ADMIN FEATURES
    // -------------------------------------------------

    public void showAccountList() {
        frame.showPanel(new AccountListPanel(this, db));
    }

    // -------------------------------------------------
    // ATTENDANCE
    // -------------------------------------------------

    public void showAttendanceInstructorList() {
        frame.showPanel(new AttendanceInstructorListPanel(this, db));
    }

    public void showAttendanceDetail(Instructor instructor) {
        frame.showPanel(new AttendanceDetailPanel(this, db, instructor));
    }

    // -------------------------------------------------
    // LEAVE REQUESTS
    // -------------------------------------------------

    public void showLeaveRequests() {
        frame.showPanel(new LeaveRequestPanel(this, db));
    }

    // -------------------------------------------------
    // CLASS SCHEDULE
    // -------------------------------------------------

    public void showClassSchedules() {
        frame.showPanel(new ClassSchedulePanel(this, db));
    }
}