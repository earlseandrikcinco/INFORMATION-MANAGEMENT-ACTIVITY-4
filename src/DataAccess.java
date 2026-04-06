import ref.ClassSchedule;
import ref.SystemUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDateTime;

public class DataAccess {
    public SystemUser getUserLogin(String key){
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
        } catch (Exception e){
            e.printStackTrace();
        }

        return classSchedule;
    }
}
