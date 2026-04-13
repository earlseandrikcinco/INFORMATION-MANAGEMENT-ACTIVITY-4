import app.DataAccess;
import ref.*;

import java.util.List;
import java.util.Scanner;

public class MainConsole {
    static Scanner input = new Scanner(System.in);
    static DataAccess access = new DataAccess();

    public static void main(String[] args) {
        while (true) {
            SystemUser user = null;

            String userName;
            do {
                System.out.print("Enter user name: ");
                userName = input.nextLine();

                if (userName.isEmpty()) {
                    System.out.println("Invalid input, user name field cannot be empty!\n");
                }

                SystemUser temp = access.getUser(userName);
                if (temp != null) {
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

                if (password.isEmpty()) {
                    System.out.println("Invalid input, password field cannot be empty");
                    continue;
                }

                if (user.getPassword().equals(password)) {
                    break;
                } else {
                    passwordAttempts--;
                    System.out.println("Incorrect password. Remaining attempts: " + passwordAttempts);
                }

                if (passwordAttempts == 0) {
                    System.out.println("Max attempts reached. Terminating program.");
                    System.exit(0);
                }
            } while (true);

            getUserDashboard(user);
        }
    }

    public static void getUserDashboard(SystemUser user) {
        String role = user.getRole();
        printWelcomeMessage(user);

        switch (role) {
            case "Admin" -> adminDashboard(user);
            case "Secretary" -> secretaryDashboard(user);
            case "DeptHead" -> deptHeadDashboard(user);
            case "Checker" -> checkerDashboard(user);
        }
    }

    public static void printWelcomeMessage(SystemUser user) {
        String name = user.getName();
        String message = "Welcome, " + name;
        int width = 62;
        int padding = (width - message.length()) / 2;

        System.out.println("=".repeat(width));
        System.out.printf("%" + (padding + message.length()) + "s%n", message);
        System.out.println("=".repeat(width));
    }

    public static void adminDashboard(SystemUser user) {
        while (true) {
            System.out.println("\n1. View details of registered accounts\n2. Check attendance records\n3. Log out");
            System.out.print("Enter action: ");
            int choice = validChoice(1, 3);

            switch (choice) {
                case 1 -> {
                    System.out.println("\n1. Instructors\n2. Department Head\n3. Secretary\n4. Checkers\n5. Go Back");
                    System.out.print("Select category: ");
                    int category = validChoice(1, 5);

                    switch (category) {
                        case 1 -> {
                            List<String> instructorList = access.getInstructorNameList();
                            for (int i = 0; i < instructorList.size(); i++) {
                                System.out.println((i + 1) + ". " + instructorList.get(i));
                            }
                            System.out.print("Select instructor: ");
                            int instructChoice = validChoice(1, instructorList.size());
                            Instructor temp = access.getInstructorDetails(instructorList.get(instructChoice - 1));
                            System.out.println(temp.toString());
                        }
                        case 2, 3 -> {
                            List<Department> departments = access.getDepartments();
                            for (int i = 0; i < departments.size(); i++) {
                                System.out.println((i + 1) + ". " + departments.get(i).getDepartmentName());
                            }
                            System.out.print("Select department: ");
                            int deptChoice = validChoice(1, departments.size());
                            int deptID = departments.get(deptChoice - 1).getDepartmentID();

                            SystemUser staff = (category == 2) ? access.getDeptHead(deptID) : access.getSecretary(deptID);
                            System.out.println(staff != null ? staff.toString() : "No record found.");
                        }
                        case 4 -> {
                            List<SystemUser> checkerList = access.getCheckers();
                            for (int i = 0; i < checkerList.size(); i++) {
                                System.out.println((i + 1) + ": " + checkerList.get(i).getName());
                            }
                            System.out.print("Select checker: ");
                            int checkerChoice = validChoice(1, checkerList.size());
                            System.out.println(checkerList.get(checkerChoice - 1).toString());
                        }
                    }
                }
                case 3 -> { if (handleLogout()) return; }
            }
        }
    }

    public static void secretaryDashboard(SystemUser user) {
        while (true) {
            System.out.println("\n1. View professor leave requests\n2. View attendance records\n3. Log out");
            System.out.print("Enter action: ");
            int choice = validChoice(1, 3);
            switch (choice) {
                case 3 -> { if (handleLogout()) return; }
            }
        }
    }

    public static void deptHeadDashboard(SystemUser user) {
        while (true) {
            System.out.println("\n1. View professor leave requests\n2. View attendance records\n3. View class schedules\n4. Log out");
            System.out.print("Enter action: ");
            int choice = validChoice(1, 4);
            switch (choice) {
                case 3 -> viewClassSchedules(user);
                case 4 -> { if (handleLogout()) return; }
            }
        }
    }

    public static void checkerDashboard(SystemUser user) {
        while (true) {
            System.out.println("\n1. View class schedules\n2. View attendance records\n3. Log out");
            System.out.print("Enter action: ");
            int choice = validChoice(1, 3);
            switch (choice) {
                case 1 -> viewClassSchedules(user);
                case 3 -> { if (handleLogout()) return; }
            }
        }
    }

    public static void viewClassSchedules(SystemUser currentUser) {
        String role = currentUser.getRole();
        if (!(role.equalsIgnoreCase("Checker") || role.equalsIgnoreCase("DeptHead"))) {
            System.out.println("Error: Access Denied.");
            return;
        }

        List<ClassSchedule> schedules = access.getAllClassSchedules();
        if (schedules.isEmpty()) {
            System.out.println("No class schedules found.");
            return;
        }

        System.out.println("\n" + "=".repeat(35) + " CLASS SCHEDULES " + "=".repeat(35));
        System.out.printf("%-10s | %-12s | %-18s | %-8s | %-20s\n", "CODE", "COURSE_NO", "TIME", "DAYS", "INSTRUCTOR");
        System.out.println("-".repeat(85));

        for (ClassSchedule s : schedules) {
            String timeRange = s.getStartTime().toString().substring(0, 5) + " - " + s.getEndTime().toString().substring(0, 5);
            String nameOnly = s.getInstructorName() != null ? s.getInstructorName().replace("Prof. ", "") : "N/A";

            System.out.printf("%-10d | %-12s | %-18s | %-8s | %-20s\n",
                    s.getClassCode(), s.getCourseNo(), timeRange, s.getDays(), nameOnly);
        }
        System.out.println("=".repeat(85));
    }

    public static int validChoice(int min, int max) {
        while (true) {
            try {
                int choice = Integer.parseInt(input.nextLine());
                if (choice >= min || choice <= max) return choice;
                System.out.println("Invalid choice. Please select between " + min + " and " + max);
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    public static boolean handleLogout() {
        System.out.print("Are you sure you want to log out? [y/n]: ");
        String response = input.nextLine().toLowerCase();
        if (!response.isEmpty() && response.charAt(0) == 'y') {
            System.out.println("Logging out...");
            return true;
        }
        return false;
    }
}