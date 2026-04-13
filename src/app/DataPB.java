package app;

import java.sql.*;

public class DataPB {
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/attendancechecker?useSSL=false&serverTimezone=UTC",
                    "root",
                    ""
            );
//            Connection conn = DriverManager.getConnection(
//                    "jdbc:mysql://localhost:3306/attendancechecker?useSSL=false&serverTimezone=UTC",
//                    "root",
//                    ""
//            );
            System.out.println("CONNECTED SUCCESSFULLY");
            return conn;
        } catch (Exception e) {
            throw new RuntimeException("DB CONNECTION FAILED", e);
        }
    }
}