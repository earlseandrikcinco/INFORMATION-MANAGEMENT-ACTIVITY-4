package app;

import gui.*;
import ref.*;

import javax.swing.*;
import java.util.Objects;

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

    public void showAccountList() {
        frame.showPanel(new AccountListPanel(this, db));
    }

    public void showCreateAccount() {
        frame.showPanel(new CreateAccountPanel(this, db, (Admin) currentUser));
    }


    public void showAttendanceInstructorList() {
        frame.showPanel(new AttendanceInstructorListPanel(this, db, currentUser));
    }

    public void showAttendanceDetail(Instructor instructor) {
        frame.showPanel(new AttendanceDetailPanel(this, db, instructor));
    }


    public void showLeaveRequests() {
        String role = currentUser.getRole();

        if (!role.equals("Secretary") && !role.equals("DeptHead")) {
            frame.showError("Insufficient privileges. This feature is for Secretaries and Department Heads only.");
            return;
        }

        frame.showPanel(new LeaveRequestPanel(this, db, currentUser));
    }

    public void showUpdateLeaveRequest() {
        frame.showPanel(new UpdateLeaveRequestPanel(this, db, currentUser));
    }


    public void showClassSchedules() {
        frame.showPanel(new ClassSchedulePanel(this, db, currentUser));
    }

    public void showCreateSchedule() {
        frame.showPanel(new CreateSchedulePanel(this, db, currentUser));
    }
}