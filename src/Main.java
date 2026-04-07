import ref.ClassSchedule;
import ref.DeptHead;
import ref.Instructor;
import ref.SystemUser;

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
        SystemUser user = null;

        // Validate user credentials in log in
        while (true){
            String userName;
            do {
                System.out.print("Enter user name: ");
                userName = input.nextLine();

                if (userName.isEmpty()){
                    System.out.println("Invalid entry, user name field cannot be empty!\n");
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

            System.out.println();
            break;
        }
        getUserDashboard(user);
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
            System.out.print("Enter action to be executed: ");

            int choice;
            while (true){
                try{
                    choice = Integer.parseInt(input.nextLine());

                    if (choice < 1 || choice > 3) {
                        System.out.println("Invalid choice. Please select a number from the given options");
                    } else {
                        break;
                    }
                } catch (Exception e){
                    System.out.println("Invalid choice. Please select a number from the given options");
                }
            }

            switch (choice){
                case 1 -> {
                    // TODO: add method to view all registered accounts
                    System.out.println("""
                            1. Instructors
                            2. Department Head
                            3. Secretary
                            4. Checkers
                            """);
                    System.out.print("Select category to view: ");
                    int category = Integer.parseInt(input.nextLine());

                    switch (category){
                        case 1 -> {
                            List<String> instructorList= access.getInstructorList();
                            int count = 1;

                            for (String name : instructorList){
                                System.out.println(count + ". " + name);
                                count++;
                            }
                            System.out.print("Select number of instructor to view: ");
                            int instructChoice = Integer.parseInt(input.nextLine());

                            Instructor temp = access.getInstructorDetails(instructorList.get(instructChoice - 1));

                            System.out.println(temp.toString());

                            System.out.print("Press enter key to continue...");
                            input.nextLine();
                        }
                        case 2 -> {
                            List<String> deptNames = access.getDepartments();
                            int count = 1;

                            for (String dept : deptNames){
                                System.out.println(count + ". " + dept);
                                count++;
                            }
                            System.out.print("Select number of department to view: ");
                            int deptChoice = Integer.parseInt(input.nextLine());

                            SystemUser deptHead = access.getDeptHead(deptNames.get(deptChoice - 1));

                            System.out.println(deptHead.toString());

                            System.out.print("Press enter key to continue...");
                            input.nextLine();
                        }
                        case 3 -> {
                            List<String> deptList = access.getDepartments();
                            int count = 1;

                            for (String dept : deptList){
                                System.out.println(count + ". " + dept);
                                count++;
                            }
                            System.out.print("Select number of department to view its secretary: ");
                            int secChoice  = Integer.parseInt(input.nextLine());

                            SystemUser secretary = access.getSecretary(deptList.get(secChoice - 1));

                            System.out.println(secretary.toString());

                            System.out.print("Press enter key to continue...");
                            input.nextLine();
                        }
                        case 4 -> {
                            // TODO: select checkers
                            List<SystemUser> checkerList = access.getCheckers();
                            int count = 1;

                            for (SystemUser temp : checkerList){
                                System.out.println(count + ": " + temp.getName());
                                count++;
                            }
                            System.out.print("Select number of checker you want to view: ");
                            int checkerChoice = Integer.parseInt(input.nextLine());
                            SystemUser checker = checkerList.get(checkerChoice - 1);

                            System.out.println(checker.toString());

                            System.out.print("Please enter key to continue...");
                            input.nextLine();
                        }
                        case 5 -> {
                            // TODO: go back to main menu
                            adminDashboard(user);
                        }
                    }
                }
                case 2 -> {
                    // TODO: add method to view attendance records
                }
                case 3 -> handleLogout();
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

        System.out.println("""
                1. View professor leave requests
                2. View attendance records
                3. Log out
                """);
        System.out.print("Enter action to be executed: ");

        int choice;
        while (true){
            try{
                choice = Integer.parseInt(input.nextLine());

                if (choice >= 1 && choice <= 3){
                    System.out.println("Invalid choice. Please select a number from the given options");
                }
                break;
            } catch (Exception e){
                System.out.println("Invalid choice. Please select a number from the given options");
            }
        }

        switch (choice){
            case 1 -> {
                // TODO: add method to view all requests for leave
            }
            case 2 -> {
                // TODO: add method to view attendance record
            }
            case 3 -> handleLogout();
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

        System.out.println("""
                1. View professor leave requests
                2. View attendance records
                3. View class schedules
                4. Log out
                """);
        System.out.print("Enter action to be executed: ");

        int choice;
        while (true){
            try{
                choice = Integer.parseInt(input.nextLine());

                if (choice >= 1 && choice <= 4){
                    System.out.println("Invalid choice. Please select a number from the given options");
                }
                break;
            } catch (Exception e){
                System.out.println("Invalid choice. Please select a number from the given options");
            }
        }

        switch (choice){
            case 1 -> {}
            case 2 -> {}
            case 3 -> viewClassSchedules(user);
            case 4 -> handleLogout();
        }
    }

    public static void checkerDashboard(SystemUser user){
        /*
            CHECKER ACTIONS
                - check class schedule
                - view attendance of prof
        */
        System.out.println("""
                1. View class schedules
                2. View attendance records
                3. Log out
                """);
        System.out.print("Enter action to be executed: ");

        int choice;
        while (true){
            try{
                choice = Integer.parseInt(input.nextLine());

                if (choice >= 1 && choice <= 3){
                    System.out.println("Invalid choice. Please select a number from the given options");
                }
                break;
            } catch (Exception e){
                System.out.println("Invalid choice. Please select a number from the given options");
            }
        }

        switch (choice){
            case 1 -> viewClassSchedules(user);
            case 2 -> {}
            case 3 -> handleLogout();
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

            System.out.printf("%-10s | %-12s | %-18s | %-8s | %-8s | %-20s\n",
                    s.getClassCode(),
                    s.getCourseNo(),
                    timeRange,
                    s.getDays(),
                    s.getRoom(),
                    nameOnly);
        }
        System.out.println("=".repeat(85));
    }

    public static void handleLogout() {
        System.out.print("Are you sure you want to log out? [y/n]: ");
        char confirm = input == null ? ' ' : input.nextLine().charAt(0);
        switch (confirm){
            case 'y' -> {
                System.out.println("Logging out. Thank you for using [App Name]");
//                        main(null);
                System.exit(0);
            }
            case 'n' -> {
                System.out.println("Log out cancelled. Now returning to main menu\n");
            }
            default -> {
                System.out.println("Invalid choice, log out cancelled. Now returning to main menu\n");
            }
        }
    }
}
