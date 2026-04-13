package app;

import java.sql.*;

public class DataPB {
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/attendancechecker_updated(1)",
                    "root",
                    ""
            );
            System.out.println("CONNECTED SUCCESSFULLY");
            return conn;
        } catch (Exception e) {
            throw new RuntimeException("DB CONNECTION FAILED", e);
        }
    }
}