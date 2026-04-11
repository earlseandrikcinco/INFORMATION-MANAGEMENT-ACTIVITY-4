package app;

import java.sql.*;

public class DataPB {
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/attendancechecker?useSSL=false&serverTimezone=UTC",
                    "root",
                    "");
        } catch (Exception e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
            return null;
        }
    }
}