import java.sql.*;
import java.util.*;

public class DataPB {
    private static Connection conn;

    public DataPB(){
    }

    public static void setConnection(){
        try{
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/attendancechecker","root","");
            System.out.println("Connected to the database successfully");
        } catch (Exception e) {
            System.out.println("Database connection failed");
            e.printStackTrace();
        }
    }
}
