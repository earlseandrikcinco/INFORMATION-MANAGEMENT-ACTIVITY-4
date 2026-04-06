import ref.ClassSchedule;
import ref.Instructor;
import ref.SystemUser;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class DataAccess {
    public SystemUser getUser(String key){
        SystemUser user = null;

        try {
            Connection conn = DataPB.setConnection();

            String sql = "SELECT * FROM system_user WHERE username = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, key);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userID = rs.getInt("userID");
                String name = rs.getString("name");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");
                String email = rs.getString("email");

                user = new SystemUser(userID, name, username, password, role, email);
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    public ClassSchedule getClassSchedule(int code){
        ClassSchedule classSchedule = null;

        try{
            Connection conn = DataPB.setConnection();

            String sql = "SELECT * FROM class_schedule WHERE classCode = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, code);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                int classCode = rs.getInt("classCode");
                String courseNo = rs.getString("courseNo");
                Time startTime = rs.getTime("startTime");
                Time endTime = rs.getTime("endTime");
                String days = rs.getString("days");
                int instructID = rs.getInt("instructID");
                String room = rs.getString("room");

                classSchedule = new ClassSchedule(classCode, courseNo, startTime, endTime, days, instructID, room);
            }
            conn.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        return classSchedule;
    }

    public List<String> getInstructorList(){
        List<String> temp = new ArrayList<>();

        try {
            Connection conn = DataPB.setConnection();

            String sql = "SELECT name FROM instructor";
            PreparedStatement stmt = conn.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                temp.add(rs.getString("name"));
            }
            conn.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        return temp;
    }

    public Instructor getInstructorDetails(String instructorName){
        Instructor temp = null;

        try {
            Connection conn = DataPB.setConnection();

            String sql = "SELECT * FROM instructor WHERE name = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                int instructID = rs.getInt("instructID");
                String name = rs.getString("name");
                String dept = rs.getString("department");

                temp = new Instructor(instructID, name, dept);
            }
            conn.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return temp;
    }

    public List<String> getDepartments(){
        List<String> deptNames = new ArrayList<>();

        try {
            Connection conn = DataPB.setConnection();

            String sql = "SELECT department FROM depthead";
            PreparedStatement stmt = conn.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                deptNames.add(rs.getString("department"));
            }
            conn.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return deptNames;
    }

    public SystemUser getDeptHead(String dept) {
        SystemUser head = null;

        try {
            Connection conn = DataPB.setConnection();

            String sql = "SELECT u.role, u.username, d.department " +
                    "FROM system_user u " +
                    "INNER JOIN depthead d ON u.userID = d.userID " +
                    "WHERE u.role = 'DeptHead' AND d.department = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, dept);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                head = getUser(rs.getString("username"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return head;
    }

    public SystemUser getSecretary(String dept){
        SystemUser sec = null;

        try {
            Connection conn = DataPB.setConnection();

            String sql = "SELECT u.role, u.username, d.department " +
                    "FROM system_user u " +
                    "INNER JOIN depthead d ON u.userID = d.userID " +
                    "WHERE u.role = 'Secretary' AND d.department = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, dept);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                sec = getUser(rs.getString("username"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sec;
    }

    public List<SystemUser> getCheckers(){
        List<SystemUser> temp = new ArrayList<>();

        try {
            Connection conn = DataPB.setConnection();

            String sql = "SELECT * FROM system_user WHERE role = 'Checker'";
            PreparedStatement stmt = conn.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                int userID = rs.getInt("userID");
                String name = rs.getString("name");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");
                String email = rs.getString("email");

                SystemUser tempUser = new SystemUser(userID, name, username, password, role, email);
                temp.add(tempUser);
            }
            conn.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return temp;
    }
}
