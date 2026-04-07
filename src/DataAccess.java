import ref.*;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Date;
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

    public List<ClassSchedule> getAllClassSchedules() {
        List<ClassSchedule> list = new ArrayList<>();
        try {
            Connection conn = DataPB.setConnection();

            String sql = "SELECT s.*, i.name FROM class_schedule s " +
                    "INNER JOIN instructor i ON s.instructID = i.instructID";

            PreparedStatement stmt = conn.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int classCode = rs.getInt("classCode");
                String courseNo = rs.getString("courseNo");
                Time startTime = rs.getTime("startTime");
                Time endTime = rs.getTime("endTime");
                String days = rs.getString("days");
                int instructID = rs.getInt("instructID");
                String instructorName = rs.getString("instructorName");
                String room = rs.getString("room");

                String name = rs.getString("name");

                ClassSchedule s = new ClassSchedule(classCode, courseNo, startTime, endTime, days, instructID, room, instructorName);
                s.setInstructorName(name);
                list.add(s);
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // to be modified: sql stmt using JOIN
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
                String instructorName = rs.getString("instructorName");
                String room = rs.getString("room");

                classSchedule = new ClassSchedule(classCode, courseNo, startTime, endTime, days, instructID, instructorName, room);
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

            stmt.setString(1, instructorName);

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

    // For DeptHead and Secretary
    public List<LeaveRequest> getAllLeaveRequests() {
        List<LeaveRequest> list = new ArrayList<>();
        try {
            Connection conn = DataPB.setConnection();
            String sql = "SELECT * FROM leave_request ORDER BY startDate DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new LeaveRequest(
                        rs.getInt("leaveReqID"),
                        rs.getInt("instructID"),
                        rs.getString("leaveType"),
                        rs.getDate("startDate"),
                        rs.getDate("endDate"),
                        rs.getString("status"),
                        rs.getInt("filedBy")
                ));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // For filtering
    public List<LeaveRequest> getLeaveRequestsByStatus(String status) {
        List<LeaveRequest> list = new ArrayList<>();
        try {
            Connection conn = DataPB.setConnection();
            String sql = "SELECT * FROM leave_request WHERE status = ? ORDER BY startDate DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new LeaveRequest(
                        rs.getInt("leaveReqID"),
                        rs.getInt("instructID"),
                        rs.getString("leaveType"),
                        rs.getDate("startDate"),
                        rs.getDate("endDate"),
                        rs.getString("status"),
                        rs.getInt("filedBy")
                ));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public LeaveRequest getLeaveRequest(int leaveReqID) {
        LeaveRequest lr = null;
        try {
            Connection conn = DataPB.setConnection();
            String sql = "SELECT * FROM leave_request WHERE leaveReqID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, leaveReqID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                lr = new LeaveRequest(
                        rs.getInt("leaveReqID"),
                        rs.getInt("instructID"),
                        rs.getString("leaveType"),
                        rs.getDate("startDate"),
                        rs.getDate("endDate"),
                        rs.getString("status"),
                        rs.getInt("filedBy")
                );
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lr;
    }


    // METHODS FOR CLASS SCHEDS
//    /**
//     * Returns all class schedules (with instructor name joined)
//     * For DeptHead & Checker
//     */
//    public List<ClassSchedule> getAllClassSchedules() {
//        List<ClassSchedule> list = new ArrayList<>();
//        try {
//            Connection conn = DataPB.setConnection();
//            String sql = "SELECT cs.*, i.name AS instructorName " +
//                    "FROM class_schedule cs " +
//                    "LEFT JOIN instructor i ON cs.instructID = i.instructID " +
//                    "ORDER BY cs.days, cs.startTime";
//            PreparedStatement stmt = conn.prepareStatement(sql);
//            ResultSet rs = stmt.executeQuery();
//            while (rs.next()) {
//                list.add(new ClassSchedule(
//                        rs.getInt("classCode"),
//                        rs.getString("courseNo"),
//                        rs.getTime("startTime"),
//                        rs.getTime("endTime"),
//                        rs.getString("days"),
//                        rs.getInt("instructID"),
//                        rs.getString("instructorName"),
//                        rs.getString("room")
//                ));
//            }
//            conn.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return list;
//    }

    /*
     * Returns all class schedules for a specific instructor
     * target sched lookup
     */
    public List<ClassSchedule> getClassSchedulesByInstructor(int instructID) {
        List<ClassSchedule> list = new ArrayList<>();
        try {
            Connection conn = DataPB.setConnection();
            String sql = "SELECT cs.*, i.name AS instructorName " +
                    "FROM class_schedule cs " +
                    "LEFT JOIN instructor i ON cs.instructID = i.instructID " +
                    "WHERE cs.instructID = ? " +
                    "ORDER BY cs.days, cs.startTime";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, instructID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new ClassSchedule(
                        rs.getInt("classCode"),
                        rs.getString("courseNo"),
                        rs.getTime("startTime"),
                        rs.getTime("endTime"),
                        rs.getString("days"),
                        rs.getInt("instructID"),
                        rs.getString("instructorName"),
                        rs.getString("room")
                ));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /*
     * Returns class schedules affected by a leave request (date range overlap)
     * Match based on instructor and days-of-week overlap within the leave period
     */
    public List<ClassSchedule> getAffectedClassSchedules(LeaveRequest lr) {
        List<ClassSchedule> list = new ArrayList<>();
        try {
            Connection conn = DataPB.setConnection();
            // Get all schedules for the instructor on leave, then filter by day overlap in Java
            String sql = "SELECT cs.*, i.name AS instructorName " +
                    "FROM class_schedule cs " +
                    "LEFT JOIN instructor i ON cs.instructID = i.instructID " +
                    "WHERE cs.instructID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, lr.getInstructID());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new ClassSchedule(
                        rs.getInt("classCode"),
                        rs.getString("courseNo"),
                        rs.getTime("startTime"),
                        rs.getTime("endTime"),
                        rs.getString("days"),
                        rs.getInt("instructID"),
                        rs.getString("instructorName"),
                        rs.getString("room")
                ));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // METHODS FOR ATTENDANCE
    /*
     * Returns all attendance records, joining class_schedule and instructor
     * so the caller can display meaningful info without extra lookups.
     *
     * By Admin
     */
    public List<Attendance> getAllAttendanceRecords() {
        List<Attendance> list = new ArrayList<>();
        try {
            Connection conn = DataPB.setConnection();
            String sql = "SELECT * FROM attendance ORDER BY date DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapAttendance(rs));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /*
     * Returns attendance records for a specific instructor (via leaveInstructID).
     *
     * By DeptHEad and Checker
     */
    public List<Attendance> getAttendanceByInstructor(int instructID) {
        List<Attendance> list = new ArrayList<>();
        try {
            Connection conn = DataPB.setConnection();
            String sql = "SELECT * FROM attendance WHERE leaveInstructID = ? ORDER BY date DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, instructID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapAttendance(rs));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /*
     * Returns attendance records for a specific class code.
     * can be used for leave approval
     */
    public List<Attendance> getAttendanceByClass(int classCode) {
        List<Attendance> list = new ArrayList<>();
        try {
            Connection conn = DataPB.setConnection();
            String sql = "SELECT * FROM attendance WHERE classCode = ? ORDER BY date DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, classCode);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapAttendance(rs));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /*
     * Returns attendance records on a specific date.
     * can be for filtering
     */
    public List<Attendance> getAttendanceByDate(Date date) {
        List<Attendance> list = new ArrayList<>();
        try {
            Connection conn = DataPB.setConnection();
            String sql = "SELECT * FROM attendance WHERE date = ? ORDER BY classCode";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDate(1, date);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapAttendance(rs));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private Attendance mapAttendance(ResultSet rs) throws Exception {
        return new Attendance(
                rs.getInt("attendID"),
                rs.getDate("date"),
                rs.getString("status"),
                rs.getString("absenceReason"),
                rs.getBoolean("isAsynchronous"),
                rs.getInt("classCode"),
                rs.getInt("checkerID"),
                rs.getInt("leaveReqID"),
                rs.getInt("leaveInstructID"),
                rs.getInt("substituteID")
        );
    }

    // METHODS FOR SUBSTITUTE
    /*
     * Returns the substitute record linked to an attendance record.
     */
    public Substitute getSubstituteByAttendance(int attendID) {
        Substitute sub = null;
        try {
            Connection conn = DataPB.setConnection();
            String sql = "SELECT * FROM substitute WHERE attendID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, attendID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                sub = new Substitute(
                        rs.getInt("substituteID"),
                        rs.getInt("attendID"),
                        rs.getInt("substituteInstructID"),
                        rs.getBoolean("isPlanned")
                );
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sub;
    }

    // INSTRUCTOR LOOKUP
    /*
     * Fetch an instructor by their instructID (primary key).
     * name lookup
     */
    public Instructor getInstructorByID(int instructID) {
        Instructor inst = null;
        try {
            Connection conn = DataPB.setConnection();
            String sql = "SELECT * FROM instructor WHERE instructID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, instructID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                inst = new Instructor(
                        rs.getInt("instructID"),
                        rs.getString("name"),
                        rs.getString("department")
                );
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inst;
    }

}
