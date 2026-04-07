import ref.*;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static Scanner input = new Scanner(System.in);
    static DataAccess access = new DataAccess();
    static Connection conn = DataPB.setConnection(); // test

    public static void main(String[] args) {
        // Validate user credentials in log in
        while (true){
            SystemUser user = null;

            String userName;
            do {
                System.out.print("Enter user name: ");
                userName = input.nextLine();

                if (userName.isEmpty()){
                    System.out.println("Invalid input, user name field cannot be empty!\n");
                }

                SystemUser temp = access.getUser(userName);
                if (temp != null){
                    user = temp;
                } else {
                    System.out.println("No data matched the given username key. Please try again.\n");
                    userName = "";
                }
            } while (userName.isEmpty());

            String password;
            int passwordAttempts = 3;
            do {
                System.out.print("Enter password: ");
                password = input.nextLine();

                if (password.isEmpty()){
                    System.out.println("Invalid input, password field cannot be empty");
                }

                if (user.getPassword().equals(password)){
                    break;
                } else {
                    System.out.println("Incorrect password. Remaining attempts: " + passwordAttempts);
                    passwordAttempts--;
                }

                if (passwordAttempts == 0){
                    System.out.println("Max number of attempts made for entering the user password. Please try again later.");
                    System.out.println("Now terminating program. Thank you!");
                    System.exit(0);
                }

            } while (password.isEmpty());

            getUserDashboard(user);
        }
    }

    public static void getUserDashboard(SystemUser user){
        String role = user.getRole();

        printWelcomeMessage(user);
        switch (role){
            case "Admin" -> adminDashboard(user);
            case "Secretary" -> secretaryDashboard(user);
            case "DeptHead" -> deptHeadDashboard(user);
            case "Checker" -> checkerDashboard(user);
        }
    }

    public static void printWelcomeMessage(SystemUser user){
        String name = user.getName();
        String message = "Welcome, " + name;

        int width = 62;
        int padding = (width - message.length()) / 2;

        System.out.println("==============================================================");
        System.out.printf("%" + (padding + message.length()) + "s%n", message);
        System.out.println("==============================================================");
    }

    public static void adminDashboard(SystemUser user){
        /*
            ADMIN ACTIONS
                - view account details for all accounts
                    - instructor
                    - dept head
                    - secretary
                    - checker
                - view attendance records
                    - select date to view attendance record of
        */

        while (true) {
            System.out.println("""
                    1. View details of registered accounts
                    2. Check attendance records
                    3. Log out
                    """);

            int choice = validChoice(1, 3, "Enter action to be executed: ");

            switch (choice){
                case 1 -> {
                    System.out.println("""
                            1. Instructors
                            2. Department Head
                            3. Secretary
                            4. Checkers
                            5. Go Back
                            """);
                    int category = validChoice(1, 5, "Select category to view: ");

                    switch (category) {
                        case 1 -> {
                            List<String> instructorList = access.getInstructorList();
                            int count = 1;

                            for (String name : instructorList) {
                                System.out.println(count + ". " + name);
                                count++;
                            }
                            int instructChoice = validChoice(1, instructorList.size(), "Select number of instructor to view: ");

                            Instructor temp = access.getInstructorDetails(instructorList.get(instructChoice - 1));

                            System.out.println(temp.toString());

                            System.out.print("Press enter key to continue...");
                            input.nextLine();
                        }
                        case 2 -> {
                            List<String> deptNames = access.getDepartments();
                            int count = 1;

                            for (String dept : deptNames) {
                                System.out.println(count + ". " + dept);
                                count++;
                            }
                            int deptChoice = validChoice(1, deptNames.size(), "Select number of department to view: ");

                            SystemUser deptHead = access.getDeptHead(deptNames.get(deptChoice - 1));

                            System.out.println(deptHead.toString());

                            System.out.print("Press enter key to continue...");
                            input.nextLine();
                        }
                        case 3 -> {
                            List<String> deptList = access.getDepartments();
                            int count = 1;

                            for (String dept : deptList) {
                                System.out.println(count + ". " + dept);
                                count++;
                            }
                            int secChoice = validChoice(1, deptList.size(), "Select number of department to view its secretary: ");

                            SystemUser secretary = access.getSecretary(deptList.get(secChoice - 1));

                            System.out.println(secretary.toString());

                            System.out.print("Press enter key to continue...");
                            input.nextLine();
                        }
                        case 4 -> {
                            List<SystemUser> checkerList = access.getCheckers();
                            int count = 1;

                            for (SystemUser temp : checkerList) {
                                System.out.println(count + ": " + temp.getName());
                                count++;
                            }
                            int checkerChoice = validChoice(1, checkerList.size(), "Select number of checker you want to view: ");
                            SystemUser checker = checkerList.get(checkerChoice - 1);

                            System.out.println(checker.toString());

                            System.out.print("Please enter key to continue...");
                            input.nextLine();
                        }
                        case 5 -> {}    // Goes back to the admin dashboard
                    }
                }
                case 2 -> {
                    List<String> instructorList = access.getInstructorList();
                    int count = 1;

                    for (String name : instructorList) {
                        System.out.println(count + ". " + name);
                        count++;
                    }

                    int instructChoice = validChoice(1, instructorList.size(), "Select number of instructor to view attendance: ");

                    Instructor temp = access.getInstructorDetails(instructorList.get(instructChoice - 1));

                    // Get attendance of instructor
                    List<Attendance> list = access.getInstructAttendance(temp);
                    int record = getAttendanceSummary(list);

                    System.out.println("--- ATTENDANCE SUMMARY ---");
                    System.out.println("Instructor: " + temp.getName());
                    System.out.println("Total number of class sessions: " + list.size());
                    System.out.println("Total number of absences: " + record);

                    System.out.println("View details of absences? [y/n]: ");
                    char viewChoice = input.nextLine().toLowerCase().charAt(0);

                    if (viewChoice == 'y'){
                        List<Attendance> absences = getAbsences(list);

                        System.out.println("ABSENCE DETAILS");
                        for (Attendance attendance : absences){
                            System.out.println(attendance.toString());
                            System.out.println();
                        }

                        System.out.println("Press enter key to continue...");
                        input.nextLine();
                    }
                }
                case 3 -> {
                    if (handleLogout()) return;
                }
            }
        }
    }

    public static void secretaryDashboard(SystemUser user){
        /*
            SECRETARY ACTIONS
                - view leave requests
                    - if approved, view affected class and declaration for asynchronous or substitute prof
                    - if pending, view affected class only
                - view attendance record
        */

        while (true) {
            System.out.println("""
                    1. View leave requests
                    2. View attendance records
                    3. Log out
                    """);

            int choice = validChoice(1, 3, "Enter action to be executed: ");

            switch (choice){
                case 1 -> {
                    // TODO: add method to view all requests for leave
                    // print list of all professors with leave requests
                    // get all leave requests for that given professor
                    System.out.println("""
                            1. View leave requests according to status
                            2. View leave request per instructor
                            3. Go Back
                            """);
                    int viewChoice = validChoice(1, 3, "Select option to view leave requests: ");

                    switch (viewChoice){
                        case 1 -> getLeaveByStatus(user);
                        case 2 -> getLeaveForInstructor(user);
                        case 3 -> {}
                    }


                }
                case 2 -> {
                    List<String> instructorList = access.getInstructorList();
                    int count = 1;

                    for (String name : instructorList) {
                        System.out.println(count + ". " + name);
                        count++;
                    }
                    int instructChoice = validChoice(1, instructorList.size(), "Select number of instructor to view attendance: ");

                    Instructor temp = access.getInstructorDetails(instructorList.get(instructChoice - 1));

                    // Get attendance of instructor
                    List<Attendance> list = access.getInstructAttendance(temp);
                    int record = getAttendanceSummary(list);

                    System.out.println("--- ATTENDANCE SUMMARY ---");
                    System.out.println("Instructor: " + temp.getName());
                    System.out.println("Total number of class sessions: " + list.size());
                    System.out.println("Total number of absences: " + record);

                    System.out.println("View details of absences? [y/n]: ");
                    char viewChoice = input.nextLine().toLowerCase().charAt(0);

                    if (viewChoice == 'y'){
                        List<Attendance> absences = getAbsences(list);

                        System.out.println("ABSENCE DETAILS");
                        for (Attendance attendance : absences){
                            System.out.println(attendance.toString());
                            System.out.println();
                        }

                        System.out.println("Press enter key to continue...");
                        input.nextLine();
                    }
                }
                case 3 -> {
                    if (handleLogout()) return;
                }
            }
        }
    }

    public static void deptHeadDashboard(SystemUser user){
        /*
            DEPT HEAD ACTIONS
                - view leave requests
                    - if approved, view affected class and declaration for asynchronous or substitute prof
                    - if pending, view affected class only
                - view attendance record
                - view class schedules
        */

        while (true) {
            System.out.println("""
                    1. View professor leave requests
                    2. View attendance records
                    3. View class schedules
                    4. Log out
                    """);

            int choice = validChoice(1, 4, "Enter action to be executed: ");

            switch (choice){
                case 1 -> {}
                case 2 -> {}
                case 3 -> viewClassSchedules(user);
                case 4 -> {
                    if (handleLogout()) return;
                }
            }
        }
    }

    public static void checkerDashboard(SystemUser user){
        /*
            CHECKER ACTIONS
                - check class schedule
                - view attendance of prof
        */
        while (true) {
            System.out.println("""
                    1. View class schedules
                    2. View attendance records
                    3. Log out
                    """);

            int choice = validChoice(1, 3, "Enter action to be executed: ");

            switch (choice){
                case 1 -> viewClassSchedules(user);
                case 2 -> {}
                case 3 -> {
                    if (handleLogout()) return;
                }
            }
        }
    }

    public static void viewClassSchedules(SystemUser currentUser) {
        String role = currentUser.getRole();
        if (!(role.equalsIgnoreCase("Checker") || role.equalsIgnoreCase("DeptHead"))) {
            System.out.println("Error: Access Denied. You do not have permission to view schedules.");
            return;
        }

        DataAccess da = new DataAccess();
        List<ClassSchedule> schedules = da.getAllClassSchedules();

        if (schedules.isEmpty()) {
            System.out.println("No class schedules found in the system.");
            return;
        }

        System.out.println("\n"+"=".repeat(35)+"CLASS SCHEDULES"+"=".repeat(35));
        System.out.printf("%-10s | %-12s | %-18s | %-8s | %-8s | %-20s\n",
                "CODE", "COURSE_NO", "TIME", "DAYS", "ROOM", "INSTRUCTOR");
        System.out.println("-".repeat(85));

        for (ClassSchedule s : schedules) {
            // Trim 08:00:00 to 08:00
            String timeRange = s.getStartTime().toString().substring(0, 5) + " - " +
                    s.getEndTime().toString().substring(0, 5);

            // Trim off pre-fix "Prof. "
            String nameOnly = s.getInstructorName().replace("Prof. ", "");

            System.out.printf("%-10d | %-12s | %-18s | %-8s | %-8s | %-20s\n",
                    s.getClassCode(),
                    s.getCourseNo(),
                    timeRange,
                    s.getDays(),
                    s.getRoom(),
                    nameOnly);
        }
        System.out.println("=".repeat(85));
    }

    public static int validChoice(int min, int max, String prompt) {
        int choice;
        while (true){
            try{
                System.out.print(prompt);
                choice = Integer.parseInt(input.nextLine());

                if (choice < min || choice > max) {
                    System.out.println("Invalid choice. Please select a number from the given options");
                } else {
                    return choice;
                }
            } catch (Exception e){
                System.out.println("Invalid choice. Please select a number from the given options");
            }
        }
    }

    public static boolean handleLogout() {
        System.out.print("Are you sure you want to log out? [y/n]: ");
        String response = input.nextLine().toLowerCase();

        if (!response.isEmpty() && response.charAt(0) == 'y') {
            System.out.println("Logging out. Thank you for using [App Name]");
            return true;
        }

        System.out.println("Log out cancelled.\n");
        return false;
    }

    public static int getAttendanceSummary(List<Attendance> list){
        int record = 0;

        for (Attendance attendance : list){
            if (attendance.getLeaveInstructorID() != 0){ // instructor is absent
                record += 1;
            }
        }
        return record;
    }

    public static List<Attendance> getAbsences(List<Attendance> list){
        List<Attendance> record = new ArrayList<>();

        for (Attendance attendance : list){
            if (attendance.getLeaveReqID() != 0){ // instructor is absent
                record.add(attendance);
            }
        }

        return record;
    }


    public static void getLeaveForInstructor(SystemUser user){
        List<Instructor> instructorList = access.getListOfProfLeave();
        int count = 1;

        for (Instructor instructor : instructorList){
            System.out.println(count + ". " + instructor.getName());
            count++;
        }
        int instructorChoice = validChoice(1, instructorList.size(), "Select instructor to view leave details: ");

        Instructor selected = instructorList.get(instructorChoice - 1);

        System.out.println("--- Instructor Filed Leave Requests ---");
        List<LeaveRequest> requests = access.getLeaveRequestPerProf(selected);

        for (LeaveRequest leave : requests){
            System.out.println(leave.toString());
            System.out.println();
        }

        System.out.print("Press enter key to continue...");
        input.nextLine();
    }

    public static void getLeaveByStatus(SystemUser user){
        System.out.println("""
                1. Allowed
                2. Pending
                3. Unauthorized
                4. Go back
                """);
        int choice = validChoice(1, 4, "Enter status to view: ");

        switch (choice){
            case 1 -> {
                System.out.println("--- Allowed Leave Requests ---");
                for (LeaveRequest request : access.getLeaveRequestsByStatus("Allowed")){
                    System.out.println(request.toString());
                    System.out.println();
                }

                System.out.print("Press enter key to continue...");
                input.nextLine();
            }
            case 2 -> {
                System.out.println("--- Pending Leave Requests ---");
                for (LeaveRequest request : access.getLeaveRequestsByStatus("Pending")){
                    System.out.println(request.toString());
                    System.out.println();
                }

                System.out.print("Press enter key to continue...");
                input.nextLine();
            }
            case 3 -> {
                System.out.println("--- Unauthorized Leave Requests ---");
                for (LeaveRequest request : access.getLeaveRequestsByStatus("Unauthorized")){
                    System.out.println(request.toString());
                    System.out.println();
                }

                System.out.print("Press enter key to continue...");
                input.nextLine();
            }
            case 4 -> {}
        }
    }
}
