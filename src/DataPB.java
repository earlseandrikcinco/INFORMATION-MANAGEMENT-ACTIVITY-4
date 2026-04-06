import ref.SystemUser;

import java.sql.*;
import java.util.*;

public class DataPB {
    private static Connection conn = null;

    public DataPB(){
    }

    public static Connection setConnection(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");

            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/attendancechecker?useSSL=false&serverTimezone=UTC","root","");
            return conn;
        } catch (Exception e) {
            System.out.println("Database connection failed");
            e.printStackTrace();
        }
        return null;
    }
}
