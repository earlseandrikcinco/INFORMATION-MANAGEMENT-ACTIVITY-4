import ref.SystemUser;

import java.sql.*;
import java.util.Scanner;

public class Main {
    static Scanner input = new Scanner(System.in);
    static DataAccess access = new DataAccess();

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
        }

        // TODO: home page method call
    }

    public static void getUserDashboard(SystemUser user){
        String role = user.getRole();

        switch (role){
            case "Admin" -> {}
            case "Secretary" -> {}
            case "DeptHead" -> {}
            case "Checker" -> {}
        }
    }

}
