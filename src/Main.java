import ref.SystemUser;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.Scanner;

public class Main {
    static Scanner input = new Scanner(System.in);
    static DataAccess access = new DataAccess();
    static Connection conn = DataPB.setConnection(); // test

    public static void main(String[] args) {
        SystemUser user = null;

        // Validate user credentials in log in
        while (true){
            String userName = "";
            do {
                System.out.print("Enter user name: ");
                userName = input.nextLine();

                if (userName.isEmpty()){
                    System.out.println("Invalid entry, user name field cannot be empty!\n");
                }

                SystemUser temp = access.getUserLogin(userName);
                if (temp != null){
                    user = temp;
                } else {
                    System.out.println("No data matched the given username key. Please try again.\n");
                    userName = "";
                }
            } while (userName.isEmpty());

            String password = "";
            int passwordAttempts = 3;
            do {
                System.out.print("Enter password: ");
                password = input.nextLine();

                if (password.isEmpty()){
                    System.out.println("Invalid input, password field cannot be empty");
                }

                if (user.getPassword().equalsIgnoreCase(password)){
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
                    // TODO: add method to view all registered accounts
                }
                case 2 -> {
                    // TODO: add method to view attendance records
                }
                case 3 -> {
                    System.out.print("Are you sure you want to log out? [y/n]: ");
                    char confirm = input.nextLine().charAt(0);
                    switch (confirm){
                        case 'y' -> {
                            System.out.println("Logging out. Thank you for using [App Name]");
//                            main(null);
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
            case 3 -> {
                System.out.print("Are you sure you want to log out? [y/n]: ");
                char confirm = input.nextLine().charAt(0);
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
                3. View class schedule
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
            case 3 -> {}
            case 4 -> {
                System.out.print("Are you sure you want to log out? [y/n]: ");
                char confirm = input.nextLine().charAt(0);
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
            case 1 -> {}
            case 2 -> {}
            case 3 -> {
                System.out.print("Are you sure you want to log out? [y/n]: ");
                char confirm = input.nextLine().charAt(0);
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
    }
}
