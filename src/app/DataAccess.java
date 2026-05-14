package app;

import ref.*;

import java.sql.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class DataAccess {
    public SystemUser getUser(String username) {
        String sql = "SELECT * FROM systemuser WHERE username = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    SystemUser user = new SystemUser(
                            rs.getInt("userID"),
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getObject("createdBy") != null ? rs.getInt("createdBy") : null
                    );

                    // Set departmentID directly from systemuser table
                    user.setDepartmentID(rs.getObject("departmentID") != null ? rs.getInt("departmentID") : null);

                    // Fetch approvalCode from the separate admin table
                    if (user.getRole().equalsIgnoreCase("Admin")) {
                        try (PreparedStatement adminStmt = conn.prepareStatement("SELECT approvalCode FROM admin WHERE adminID = ?")) {
                            adminStmt.setInt(1, user.getUserID());
                            try (ResultSet adminRs = adminStmt.executeQuery()) {
                                if (adminRs.next()) {
                                    user.setApprovalCode(adminRs.getString("approvalCode"));
                                }
                            }
                        }
                    }
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<CheckerDetail> getCheckerDetails(int userID) {
        List<CheckerDetail> list = new ArrayList<>();
        String sql = "SELECT cd.*, su.name AS checkerName " +
                "FROM checkerdetails cd " +
                "JOIN systemuser su ON cd.checkerID = su.userID " +
                "WHERE su.userID = ? " +
                "ORDER BY su.name, cd.day, cd.shiftStart";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userID); // This must be outside the try-with-resources parens

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ref.CheckerDetail cd = new ref.CheckerDetail(
                            rs.getInt("checkerID"), // Corrected from checkedBy to match your constructor
                            rs.getInt("scheduleID"),
                            rs.getTime("shiftStart"),
                            rs.getTime("shiftEnd"),
                            rs.getString("building"),
                            rs.getString("floor"),
                            rs.getString("day")
                    );
                    cd.setCheckerName(rs.getString("checkerName"));
                    list.add(cd);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list; // Return the variable 'list', not the class 'CheckerDetail'
    }

    public List<SystemUser> getCheckers() {
        List<SystemUser> checkers = new ArrayList<>();
        // Simply filter the collapsed table by the 'Checker' role
        String sql = "SELECT username FROM systemuser WHERE role = 'Checker' ORDER BY name ASC";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Reuse your existing getUser method to handle the logic for each checker
                SystemUser checker = getUser(rs.getString("username"));
                if (checker != null) {
                    checkers.add(checker);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return checkers;
    }

    public SystemUser getSystemUserByRoleAndDept(String role, int deptID) {
        String sql = "SELECT username FROM systemuser WHERE role = ? AND departmentID = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            stmt.setInt(2, deptID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return getUser(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Department> getDepartments() {
        List<Department> depts = new ArrayList<>();
        String sql = "SELECT * FROM department ORDER BY departmentName";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                depts.add(new Department(
                        rs.getInt("departmentID"),
                        rs.getString("departmentName"),
                        rs.getString("school")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return depts;
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM room ORDER BY building, floor";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                rooms.add(new Room(
                        rs.getInt("roomID"),
                        rs.getString("floor"),
                        rs.getString("building"),
                        rs.getInt("capacity"),
                        rs.getString("roomType")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public List<ClassSchedule> getAllClassSchedules() {
        List<ClassSchedule> list = new ArrayList<>();
        String sql =
                "SELECT s.*, i.name AS instructorName " +
                        "FROM classschedule s " +
                        "LEFT JOIN instructor i ON s.instructID = i.instructID " +
                        "ORDER BY s.startTime";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ClassSchedule schedule = new ClassSchedule(
                        rs.getString("classCode"),
                        rs.getString("courseNo"),
                        rs.getTime("startTime"),
                        rs.getTime("endTime"),
                        rs.getString("days"),
                        (Integer) rs.getObject("instructID"),
                        (Integer) rs.getObject("roomID"),
                        (Integer) rs.getObject("assignedChecker")
                );
                schedule.setInstructorName(rs.getString("instructorName"));
                list.add(schedule);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ClassSchedule> getClassSchedulesByDept(int deptID) {
        List<ClassSchedule> list = new ArrayList<>();

        try (Connection conn = DataPB.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL sp_GetClassSchedulesByDept(?)}")) {

            cs.setInt(1, deptID);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule schedule = new ClassSchedule(
                            rs.getString("classCode"),
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("instructID"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedChecker")
                    );
                    schedule.setInstructorName(rs.getString("instructorName"));
                    list.add(schedule);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ClassSchedule> getClassSchedulesByRoom(int roomID) {
        List<ClassSchedule> list = new ArrayList<>();

        try (Connection conn = DataPB.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL sp_GetClassSchedulesByRoom(?)}")) {

            cs.setInt(1, roomID);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule schedule = new ClassSchedule(
                            rs.getString("classCode"),
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("instructID"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedChecker")
                    );
                    schedule.setInstructorName(rs.getString("instructorName"));
                    list.add(schedule);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ClassSchedule> getClassSchedulesByInstructor(int instructID) {
        List<ClassSchedule> list = new ArrayList<>();

        try (Connection conn = DataPB.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL sp_GetClassSchedulesByInstructor(?)}")) {

            cs.setInt(1, instructID);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule schedule = new ClassSchedule(
                            rs.getString("classCode"),
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("instructID"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedChecker")
                    );
                    schedule.setInstructorName(rs.getString("instructorName"));
                    list.add(schedule);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ClassSchedule> getClassSchedulesByTimeRange(String dayCode, Time start, Time end) {
        List<ClassSchedule> list = new ArrayList<>();

        try (Connection conn = DataPB.getConnection();
             CallableStatement cs = conn.prepareCall("{CALL sp_GetClassSchedulesByTimeRange(?, ?, ?)}")) {

            cs.setString(1, dayCode);
            cs.setTime(2, start);
            cs.setTime(3, end);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule schedule = new ClassSchedule(
                            rs.getString("classCode"),
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("instructID"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedChecker")
                    );
                    schedule.setInstructorName(rs.getString("instructorName"));
                    list.add(schedule);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<String> getInstructorNameList() {
        List<String> instructors = new ArrayList<>();
        String sql = "SELECT name FROM instructor ORDER BY name";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                instructors.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return instructors;
    }

    public Instructor getInstructorDetails(String instructorName) {
        String sql = "SELECT * FROM instructor WHERE name = ?";
        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, instructorName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Instructor(
                            rs.getInt("instructID"),
                            rs.getString("name"),
                            rs.getInt("departmentID")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Attendance> getUnauthorizedAbsences(int instructID) {
        List<Attendance> list = new ArrayList<>();
        String sql = """
        SELECT a.*, cs.instructID AS assignedInstructID
        FROM attendance a
        JOIN classschedule cs
            ON a.classCode = cs.classCode
        WHERE cs.instructID = ?
          AND instructorStatus = 'Absent'
          AND leaveRequestID IS NULL
        """;

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Attendance(
                            rs.getInt("attendanceID"),
                            rs.getDate("startDate"),
                            rs.getDate("endDate"),
                            rs.getString("instructorStatus"),
                            rs.getString("remarks"),
                            rs.getString("classCode"),
                            rs.getInt("assignedInstructID"),
                            (Integer) rs.getObject("actualInstructID"),
                            (Integer) rs.getObject("leaveRequestID"),
                            rs.getObject("checkedBy") != null ? rs.getInt("checkedBy") : 0
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Attendance> getAttendanceByDate(Date date) {
        List<Attendance> list = new ArrayList<>();
        String sql = """
        SELECT a.*, cs.instructID AS assignedInstructID
        FROM attendance a
        JOIN classschedule cs ON a.classCode = cs.classCode
        WHERE a.startDate = ?
        """;

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Attendance(
                            rs.getInt("attendanceID"),
                            rs.getDate("startDate"),
                            rs.getDate("endDate"),
                            rs.getString("instructorStatus"),
                            rs.getString("remarks"),
                            rs.getString("classCode"),
                            rs.getInt("assignedInstructID"),
                            (Integer) rs.getObject("actualInstructID"),
                            (Integer) rs.getObject("leaveRequestID"),
                            rs.getObject("checkedBy") != null ? rs.getInt("checkedBy") : 0
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<SystemUser> getAllAccounts() {
        List<SystemUser> users = new ArrayList<>();
        String sql = "SELECT * FROM systemuser";

        try (Connection conn = DataPB.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(new SystemUser(
                        rs.getInt("userID"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getInt("createdBy")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<LeaveRequest> getLeaveRequestsByInstructor(int instructorID) {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = "SELECT lr.*, i.name AS instructorName " +
                "FROM leaverequest lr " +
                "JOIN instructor i ON lr.instructID = i.instructID " +
                "WHERE lr.instructID = ? ORDER BY lr.startDate DESC";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructorID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LeaveRequest lr = new LeaveRequest(
                            rs.getInt("leaveRequestID"),
                            rs.getString("leaveType"),
                            rs.getDate("startDate"),
                            rs.getDate("endDate"),
                            rs.getString("status"),
                            rs.getString("leaveReason"),
                            rs.getInt("instructID"),
                            rs.getInt("approvedBy")
                    );
                    lr.setInstructorName(rs.getString("instructorName"));
                    list.add(lr);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<LeaveRequest> getLeaveRequestsByDept(int deptID) {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = "SELECT lr.*, i.name AS instructorName " +
                "FROM leaverequest lr " +
                "JOIN instructor i ON lr.instructID = i.instructID " +
                "WHERE i.departmentID = ? " +
                "ORDER BY lr.startDate DESC";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deptID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LeaveRequest lr = new LeaveRequest(
                            rs.getInt("leaveRequestID"),
                            rs.getString("leaveType"),
                            rs.getDate("startDate"),
                            rs.getDate("endDate"),
                            rs.getString("status"),
                            rs.getString("leaveReason"),
                            rs.getInt("instructID"),
                            rs.getInt("approvedBy")
                    );
                    lr.setInstructorName(rs.getString("instructorName"));
                    lr.setLeaveReason(rs.getString("leaveReason"));
                    list.add(lr);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<LeaveRequest> getLeaveRequestsByStatusAndDept(String status, int deptID) {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = "SELECT lr.*, i.name AS instructorName " +
                "FROM leaverequest lr " +
                "JOIN instructor i ON lr.instructID = i.instructID " +
                "WHERE lr.status = ? AND i.departmentID = ? " +
                "ORDER BY lr.startDate DESC";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, deptID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LeaveRequest lr = new LeaveRequest(
                            rs.getInt("leaveRequestID"),
                            rs.getString("leaveType"),
                            rs.getDate("startDate"),
                            rs.getDate("endDate"),
                            rs.getString("status"),
                            rs.getString("leaveReason"),
                            rs.getInt("instructID"),
                            rs.getInt("approvedBy")
                    );
                    lr.setInstructorName(rs.getString("instructorName"));
                    lr.setLeaveReason(rs.getString("leaveReason"));
                    list.add(lr);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<LeaveRequest> getLeaveRequestsByInstructorAndDept(int instructorID, int deptID) {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = "SELECT lr.*, i.name AS instructorName " +
                "FROM leaverequest lr " +
                "JOIN instructor i ON lr.instructID = i.instructID " +
                "WHERE lr.instructID = ? AND i.departmentID = ? " +
                "ORDER BY lr.startDate DESC";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructorID);
            stmt.setInt(2, deptID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LeaveRequest lr = new LeaveRequest(
                            rs.getInt("leaveRequestID"),
                            rs.getString("leaveType"),
                            rs.getDate("startDate"),
                            rs.getDate("endDate"),
                            rs.getString("status"),
                            rs.getString("leaveReason"),
                            rs.getInt("instructID"),
                            rs.getInt("approvedBy")
                    );
                    lr.setInstructorName(rs.getString("instructorName"));
                    lr.setLeaveReason(rs.getString("leaveReason"));
                    list.add(lr);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Attendance> getAttendanceByInstructor(int instructID) {
        List<Attendance> list = new ArrayList<>();

        String sql = "SELECT a.*, cs.instructID AS assignedInstructID " +
                "FROM attendance a " +
                "JOIN classschedule cs ON a.classCode = cs.classCode " +
                "WHERE cs.instructID = ? ORDER BY a.startDate DESC";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Attendance(
                            rs.getInt("attendanceID"),
                            rs.getDate("startDate"),
                            rs.getDate("endDate"),
                            rs.getString("instructorStatus"),
                            rs.getString("remarks"),
                            rs.getString("classCode"),
                            rs.getInt("assignedInstructID"),
                            (Integer) rs.getObject("actualInstructID"),
                            (Integer) rs.getObject("leaveRequestID"),
                            rs.getInt("checkedBy")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ClassSchedule> getClassSchedules() {
        List<ClassSchedule> list = new ArrayList<>();
        String sql = "SELECT * FROM classschedule";

        try (Connection conn = DataPB.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new ClassSchedule(
                        rs.getString("classCode") /* classCode is VARCHAR */,
                        rs.getString("courseNo"),
                        rs.getTime("startTime"),
                        rs.getTime("endTime"),
                        rs.getString("days"),
                        (Integer) rs.getObject("instructID"),
                        (Integer) rs.getObject("roomID"),
                        (Integer) rs.getObject("assignedChecker")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ClassSchedule> getAllClassSchedulesByInstructor(int instructorID) {
        List<ClassSchedule> list = new ArrayList<>();
        String sql = "SELECT * FROM classschedule WHERE instructID = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructorID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ClassSchedule(
                            rs.getString("classCode") /* classCode is VARCHAR */,
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("instructID"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedChecker")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getPresentCount(int instructorID) {
        String sql = """
        SELECT COUNT(*) AS total
        FROM attendance a
        JOIN classschedule cs ON a.classCode = cs.classCode
        WHERE cs.instructID = ? AND a.instructorStatus = 'Present'
        """;

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructorID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getAbsenceCount(int instructorID) {
        String sql = """
        SELECT COUNT(*) AS total
        FROM attendance a
        JOIN classschedule cs ON a.classCode = cs.classCode
        WHERE cs.instructID = ? AND a.instructorStatus = 'Absent'
        """;

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructorID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Instructor> getInstructors() {
        List<Instructor> list = new ArrayList<>();

        String sql = "SELECT DISTINCT i.instructID, i.name, i.departmentID, d.departmentName " +
                "FROM instructor i " +
                "JOIN department d ON d.departmentID = i.departmentID " +
                "ORDER BY i.name";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Instructor instructor = new Instructor(
                            rs.getInt("instructID"),
                            rs.getString("name"),
                            rs.getInt("departmentID"));

                    instructor.setDepartmentName(rs.getString("departmentName"));
                    list.add(instructor);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Instructor> getInstructorsByDept(int deptID) {
        List<Instructor> list = new ArrayList<>();

        String sql = "SELECT DISTINCT i.instructID, i.name, i.departmentID, d.departmentName " +
                "FROM instructor i " +
                "JOIN department d ON d.departmentID = i.departmentID " +
                "WHERE i.departmentID = ? " +
                "ORDER BY i.name";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, deptID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Instructor instructor = new Instructor(
                            rs.getInt("instructID"),
                            rs.getString("name"),
                            rs.getInt("departmentID"));

                    instructor.setDepartmentName(rs.getString("departmentName"));
                    list.add(instructor);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean insertClassSchedule(ClassSchedule cs, List<ClassSchedule> conflictHolder) {
        boolean hasConflict = hasScheduleConflict(
                cs.getClassCode(),
                cs.getRoomID(),
                cs.getInstructID(),
                cs.getDays(),
                cs.getStartTime(),
                cs.getEndTime()
        );

        if (hasConflict) {

            // Optional:
            // still populate conflictHolder if caller expects details
            if (conflictHolder != null) {
                conflictHolder.clear();

                // fallback detail query
                conflictHolder.addAll(fetchScheduleConflictDetails(
                        cs.getClassCode(),
                        cs.getRoomID(),
                        cs.getInstructID(),
                        cs.getDays(),
                        cs.getStartTime(),
                        cs.getEndTime()
                ));
            }

            return false;
        }

        String sql = "INSERT INTO classschedule " +
                "(classCode, courseNo, startTime, endTime, days, roomID, instructID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cs.getClassCode()); // Changed setInt to setString
            stmt.setString(2, cs.getCourseNo());
            stmt.setTime(3, cs.getStartTime());
            stmt.setTime(4, cs.getEndTime());
            stmt.setString(5, cs.getDays());
            stmt.setObject(6, cs.getRoomID());
            stmt.setObject(7, cs.getInstructID());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ClassSchedule> fetchScheduleConflictDetails(
            String classCode,
            Integer roomID,
            Integer instructID,
            String days,
            Time start,
            Time end
    ) {

        List<ClassSchedule> conflicts = new ArrayList<>();

        String sql = """
        SELECT cs.*, i.name AS instructorName
        FROM classschedule cs
        LEFT JOIN instructor i
            ON cs.instructID = i.instructID
        WHERE cs.classCode <> ?
          AND (
                (cs.roomID = ?)
                OR
                (cs.instructID = ?)
              )
          AND cs.startTime < ?
          AND cs.endTime > ?
        """;

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, classCode);

            if (roomID != null) stmt.setInt(2, roomID);
            else stmt.setNull(2, Types.INTEGER);

            if (instructID != null) stmt.setInt(3, instructID);
            else stmt.setNull(3, Types.INTEGER);

            stmt.setTime(4, end);
            stmt.setTime(5, start);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    if (!daysOverlap(rs.getString("days"), days)) {
                        continue;
                    }

                    ClassSchedule conflict = new ClassSchedule(
                            rs.getString("classCode"),
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("instructID"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedChecker")
                    );

                    conflict.setInstructorName(rs.getString("instructorName"));

                    conflicts.add(conflict);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conflicts;
    }

    public boolean addSystemUser(SystemUser user) {
        String sqlUser = "INSERT INTO systemuser (name, username, email, password, role, createdBy, departmentID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataPB.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmtUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                stmtUser.setString(1, user.getName());
                stmtUser.setString(2, user.getUsername());
                stmtUser.setString(3, user.getEmail());
                stmtUser.setString(4, user.getPassword());

                String role = (user.getRole() != null) ? user.getRole() : "";
                stmtUser.setString(5, role.toUpperCase());

                if (user.getCreatedBy() != null) {
                    stmtUser.setInt(6, user.getCreatedBy());
                } else {
                    stmtUser.setNull(6, java.sql.Types.INTEGER);
                }

                if (user.getDepartmentID() != null && user.getDepartmentID() != 0) {
                    stmtUser.setInt(7, user.getDepartmentID());
                } else {
                    stmtUser.setNull(7, java.sql.Types.INTEGER);
                }

                if (stmtUser.executeUpdate() == 0) throw new SQLException("User creation failed.");

                try (ResultSet rs = stmtUser.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newID = rs.getInt(1);
                        if (role.equalsIgnoreCase("Admin")) {
                            try (PreparedStatement st = conn.prepareStatement("INSERT INTO admin (adminID, approvalCode) VALUES (?, ?)")) {
                                st.setInt(1, newID);
                                st.setString(2, user.getApprovalCode());
                                st.executeUpdate();
                            }
                        } else if (role.equalsIgnoreCase("Checker")) {

                            try (PreparedStatement st = conn.prepareStatement("INSERT INTO checker (checkerID) VALUES (?)")) {
                                st.setInt(1, newID);
                                st.executeUpdate();
                            }
                        }
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertLeaveRequest(LeaveRequest lr) {
        String sql = "INSERT INTO leaverequest (instructID, leaveType, startDate, endDate, status, leaveReason) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, lr.getInstructID());
            stmt.setString(2, lr.getLeaveType());
            stmt.setDate(3, lr.getStartDate());
            stmt.setDate(4, lr.getEndDate());
            stmt.setString(5, lr.getStatus());
            stmt.setString(6, lr.getLeaveReason());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Attendance> getAllAttendance() {

        List<Attendance> list = new ArrayList<>();

        try (Connection conn = DataPB.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_GetAllAttendance()}");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Attendance attendance = new Attendance(
                        rs.getInt("attendanceID"),
                        rs.getDate("startDate"),
                        rs.getDate("endDate"),
                        rs.getString("instructorStatus"),
                        rs.getString("remarks"),
                        rs.getString("classCode"),
                        rs.getInt("assignedInstructID"),
                        (Integer) rs.getObject("actualInstructID"),
                        (Integer) rs.getObject("leaveRequestID"),
                        (Integer) rs.getObject("checkedBy")
                );

                list.add(attendance);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean updateLeaveStatus(int instructID, int reqID, String status, int adminID) {

        String sql = """
        UPDATE leaverequest
        SET status = ?, approvedBy = ?
        WHERE instructID = ?
          AND leaveRequestID = ?
        """;

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, adminID);
            stmt.setInt(3, instructID);
            stmt.setInt(4, reqID);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean assignInstructorToClass(String classCode, int instructID) {
        String sql = "UPDATE classschedule SET instructID = ? WHERE classCode = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructID);
            stmt.setString(2, classCode);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resolveLeaveRequest(int instructID,
                                       int leaveRequestID,
                                       String newStatus,
                                       int reviewerID) {

        String sql = """
        UPDATE leaverequest
        SET status = ?, approvedBy = ?
        WHERE instructID = ?
          AND leaveRequestID = ?
        """;

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, reviewerID);
            stmt.setInt(3, instructID);
            stmt.setInt(4, leaveRequestID);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean linkAttendanceToLeave(String classCode, Date date, int leaveRequestID) {
        String sql = "UPDATE attendance SET leaveRequestID = ? " +
                "WHERE classCode = ? AND startDate = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, leaveRequestID);
            stmt.setString(2, classCode);
            stmt.setDate(3, date);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getDayLetter(int dayOfWeek) {
        return switch (dayOfWeek) {
            case java.util.Calendar.MONDAY -> "M";
            case java.util.Calendar.TUESDAY -> "T";
            case java.util.Calendar.WEDNESDAY -> "W";
            case java.util.Calendar.THURSDAY -> "Th";
            case java.util.Calendar.FRIDAY -> "F";
            case java.util.Calendar.SATURDAY -> "S";
            default -> "";
        };
    }

    public boolean isUsernameAvailable(String username) {
        String sql = "SELECT fn_IsUsernameAvailable(?)";
        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getBoolean(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public String getRoomDescription(int roomID) {
        String sql = "SELECT fn_GetRoomDescription(?)";
        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "N/A";
    }

    public boolean validateAdminCode(int adminID, String code) {
        String sql = "SELECT fn_ValidateAdminApproval(?, ?)";
        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, adminID);
            stmt.setString(2, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getBoolean(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean syncLeaveToAttendance(int leaveRequestID) {
        String sql = "{CALL sp_SyncLeaveToAttendance(?)}";

        try (Connection conn = DataPB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, leaveRequestID);
            stmt.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // TODO java.sql.SQLException: Illegal mix of collations (utf8mb4_general_ci,IMPLICIT) and (utf8mb4_0900_ai_ci,IMPLICIT) for operation '='
    public boolean upsertAttendance(
            String classCode,
            Date date,
            String status,
            int checkerID,
            Integer actualInstructorID,
            Integer leaveRequestID,
            String remarks) {

        String sql = "{CALL sp_UpsertAttendance(?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DataPB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, classCode);

            stmt.setDate(2, date);

            stmt.setString(3, status);

            stmt.setInt(4, checkerID);

            if (actualInstructorID != null)
                stmt.setInt(5, actualInstructorID);
            else
                stmt.setNull(5, Types.INTEGER);

            if (leaveRequestID != null)
                stmt.setInt(6, leaveRequestID);
            else
                stmt.setNull(6, Types.INTEGER);

            stmt.setString(7, remarks);
            stmt.execute();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public LeaveRequest getApprovedLeaveForInstructorOnDate(int instructorID, java.sql.Date date) {
        String sql =
                "SELECT lr.*, i.name AS instructorName " +
                        "FROM   leaverequest lr " +
                        "JOIN   instructor   i  ON lr.instructID = i.instructID " +
                        "WHERE  lr.instructID = ? " +
                        "  AND  lr.status     = 'Approved' " +
                        "  AND  lr.startDate  <= ? " +
                        "  AND  lr.endDate    >= ? " +
                        "LIMIT 1";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructorID);
            stmt.setDate(2, date);
            stmt.setDate(3, date);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LeaveRequest lr = new LeaveRequest(
                            rs.getInt("leaveRequestID"),
                            rs.getString("leaveType"),
                            rs.getDate("startDate"),
                            rs.getDate("endDate"),
                            rs.getString("status"),
                            rs.getString("leaveReason"),
                            rs.getInt("instructID"),
                            (Integer) rs.getObject("approvedBy")
                    );
                    lr.setInstructorName(rs.getString("instructorName"));
                    return lr;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Instructor> getAvailableSubstitutesForClass(
            int assignedInstructID,
            String absentDays,
            java.sql.Time absentStart,
            java.sql.Time absentEnd) {

        // Build a REGEXP pattern that matches any day token found in absentDays.
        // We must handle the T / Th ambiguity exactly like hasDay() does:
        //   - "Th" → literal substring "Th"
        //   - "T"  → "T" not followed by "h"  →  T([^h]|$)
        // We OR all matching day patterns together.
        StringBuilder dayRegex = new StringBuilder();
        String[] dayTokens = {"M", "Th", "W", "F", "S", "T"};
        for (String day : dayTokens) {
            if (hasDay(absentDays, day)) {
                if (dayRegex.length() > 0) dayRegex.append("|");
                if (day.equals("Th")) {
                    dayRegex.append("Th");
                } else if (day.equals("T")) {
                    dayRegex.append("T([^h]|$)");
                } else {
                    dayRegex.append(day);
                }
            }
        }

        // If the absent class has no recognisable days, return everyone (minus assignedInstructor).
        if (dayRegex.length() == 0) {
            return getAvailableSubstitutes(assignedInstructID);
        }

        // Subquery: instructor IDs who are busy during the absent class's slot.
        // A conflict requires BOTH day overlap AND time overlap.
        String sql =
                "SELECT i.instructID, i.name, i.departmentID, d.departmentName " +
                        "FROM   instructor i " +
                        "JOIN   department d ON d.departmentID = i.departmentID " +
                        "WHERE  i.instructID <> ? " +                     // exclude assigned instructor
                        "  AND  i.instructID NOT IN ( " +
                        "       SELECT cs.instructID " +
                        "       FROM   classschedule cs " +
                        "       WHERE  cs.instructID IS NOT NULL " +
                        "         AND  cs.days REGEXP ? " +               // day overlap
                        "         AND  cs.startTime < ? " +               // time overlap: their class starts before absent ends
                        "         AND  cs.endTime   > ? " +               // time overlap: their class ends   after absent starts
                        "  ) " +
                        "ORDER BY i.name";

        List<Instructor> list = new ArrayList<>();
        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, assignedInstructID);
            stmt.setString(2, dayRegex.toString());
            stmt.setTime(3, absentEnd);     // their startTime < absentEnd
            stmt.setTime(4, absentStart);   // their endTime   > absentStart

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Instructor inst = new Instructor(
                            rs.getInt("instructID"),
                            rs.getString("name"),
                            rs.getInt("departmentID")
                    );
                    inst.setDepartmentName(rs.getString("departmentName"));
                    list.add(inst);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getInstructorAttendanceRate(int instructID) {
        String sql = "SELECT fn_GetInstructorAttendanceRate(?)";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public boolean hasScheduleConflict(String classCode, Integer roomID, Integer instructID, String days, Time start, Time end) {
        String sql = "SELECT fn_CheckScheduleConflict(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, classCode);
            if (roomID != null) stmt.setInt(2, roomID); else stmt.setNull(2, Types.INTEGER);
            if (instructID != null) stmt.setInt(3, instructID); else stmt.setNull(3, Types.INTEGER);
            stmt.setString(4, days);
            stmt.setTime(5, start);
            stmt.setTime(6, end);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean daysOverlap(String d1, String d2) {
        if (d1 == null || d2 == null) return false;

        String[] dayPatterns = {"M", "Th", "W", "F", "S", "T"};

        for (String day : dayPatterns) {
            if (hasDay(d1, day) && hasDay(d2, day)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDay(String schedule, String day) {
        if (day.equals("Th")) return schedule.contains("Th");

        if (day.equals("T")) {
            return schedule.replace("Th", "").contains("T");
        }

        return schedule.contains(day);
    }

    public ClassSchedule getScheduleWithDetails(String classCode) {

        String sql =
                "SELECT cs.*, " +
                        "       i.name AS instructorName, " +
                        "       su.name AS checkerName, " +
                        "       r.building, r.floor, r.roomType " +
                        "FROM classschedule cs " +
                        "LEFT JOIN instructor i ON cs.instructID = i.instructID " +
                        "LEFT JOIN checker ch ON cs.assignedChecker = ch.checkerID " +
                        "LEFT JOIN systemuser su ON ch.checkerID = su.userID " +
                        "LEFT JOIN room r ON cs.roomID = r.roomID " +
                        "WHERE cs.classCode = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, classCode);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {

                    ClassSchedule cs = new ClassSchedule(
                            rs.getString("classCode") /* classCode is VARCHAR */,
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("instructID"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedChecker")
                    );

                    cs.setAssignedChecker((Integer) rs.getObject("assignedChecker"));
                    cs.setInstructorName(rs.getString("instructorName"));
                    cs.setCheckerName(rs.getString("checkerName"));

                    // Room description
                    String building = rs.getString("building");
                    String floor = rs.getString("floor");
                    String roomType = rs.getString("roomType");

                    if (building != null) {
                        cs.setRoomDescription(building + " – " + floor + " (" + roomType + ")");
                    }

                    return cs;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean updateClassScheduleAssignments(String classCode, Integer instructID, Integer checkerID) {
        String sql = "UPDATE classschedule SET instructID = ?, assignedChecker = ? WHERE classCode = ?";
        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (instructID == null) stmt.setNull(1, java.sql.Types.INTEGER);
            else stmt.setInt(1, instructID);

            if (checkerID == null) stmt.setNull(2, java.sql.Types.INTEGER);
            else stmt.setInt(2, checkerID);

            stmt.setString(3, classCode);

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<ClassSchedule> fetchNeedingAttention(String sql, Integer deptID) {
        List<ClassSchedule> list = new ArrayList<>();
        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (deptID != null) stmt.setInt(1, deptID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule cs = new ClassSchedule(
                            rs.getString("classCode") /* classCode is VARCHAR */,
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("instructID"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedChecker")
                    );
                    cs.setAssignedChecker((Integer) rs.getObject("assignedChecker"));
                    cs.setInstructorName(rs.getString("instructorName"));
                    list.add(cs);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }


    public List<ClassSchedule> getSchedulesNeedingAttention() {
        return fetchNeedingAttention(
                "SELECT cs.*, i.name AS instructorName " +
                        "FROM   classschedule cs " +
                        "LEFT JOIN instructor i ON cs.instructID = i.instructID " +
                        "WHERE  cs.instructID IS NULL OR cs.assignedChecker IS NULL " +
                        "ORDER BY cs.startTime",
                null
        );
    }

    public List<ClassSchedule> getSchedulesNeedingAttentionByDept(int deptID) {
        return fetchNeedingAttention(
                "SELECT cs.*, i.name AS instructorName " +
                        "FROM   classschedule cs " +
                        "LEFT JOIN instructor i ON cs.instructID = i.instructID " +
                        "WHERE  (cs.instructID IS NULL OR cs.assignedChecker IS NULL) " +
                        "  AND  (i.departmentID = ? OR cs.instructID IS NULL) " +
                        "ORDER BY cs.startTime",
                deptID
        );
    }

    // fetches attendance rows linked to leave request
    public List<AffectedClass> getAffectedClassesByLeave(int leaveRequestID) {
        List<AffectedClass> list = new ArrayList<>();
        String sql =
                "SELECT a.attendanceID, a.startDate, a.instructorStatus, " +
                        "       cs.courseNo, cs.startTime, cs.endTime, " +
                        "       r.building, r.floor, " +
                        "       sub.name AS substituteName " +
                        "FROM attendance a " +
                        "JOIN classschedule cs ON a.classCode = cs.classCode " +
                        "LEFT JOIN room r ON cs.roomID = r.roomID " +
                        "LEFT JOIN instructor sub ON a.actualInstructID = sub.instructID " +
                        "WHERE a.leaveRequestID = ? " +
                        "ORDER BY a.startDate, cs.startTime";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, leaveRequestID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new AffectedClass(
                            rs.getInt("attendanceID"),
                            rs.getDate("startDate"),
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("building"),
                            rs.getString("floor"),
                            rs.getString("instructorStatus"),
                            rs.getString("substituteName") // null if no substitute
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean assignSubstitute(int attendanceID, int substituteInstructID) {
        String sql =
                "UPDATE attendance " +
                        "SET actualInstructID = ?, instructorStatus = 'Substituted' " +
                        "WHERE attendanceID = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, substituteInstructID);
            stmt.setInt(2, attendanceID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean makeClassAsynchronous(int attendanceID) {
        String sql =
                "UPDATE attendance " +
                        "SET instructorStatus = 'Asynchronous', actualInstructID = NULL " +
                        "WHERE attendanceID = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attendanceID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // excludes instructor on leave so they wont appear in substiture picker
    public List<Instructor> getAvailableSubstitutes(int excludeInstructID) {
        List<Instructor> list = new ArrayList<>();
        String sql =
                "SELECT i.instructID, i.name, i.departmentID, d.departmentName " +
                        "FROM instructor i " +
                        "JOIN department d ON d.departmentID = i.departmentID " +
                        "WHERE i.instructID <> ? " +
                        "ORDER BY i.name";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, excludeInstructID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Instructor i = new Instructor(
                            rs.getInt("instructID"),
                            rs.getString("name"),
                            rs.getInt("departmentID")
                    );
                    i.setDepartmentName(rs.getString("departmentName"));
                    list.add(i);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean createUser(SystemUser user) {
        String sql = "INSERT INTO systemuser " +
                "(name, username, email, password, role, createdBy) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole());

            if (user.getCreatedBy() == null) {
                stmt.setNull(6, Types.INTEGER);
            } else {
                stmt.setInt(6, user.getCreatedBy());
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT userID FROM systemuser WHERE username = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Attendance> getAttendanceRecords() {
        List<Attendance> list = new ArrayList<>();

        String sql = """
        SELECT a.*,
               cs.courseNo,
               cs.instructID AS assignedInstructID,
               i.name AS instructorName,
               lr.leaveRequestID,
               lr.leaveType,
               lr.status AS leaveStatus,
               lr.leaveReason AS reason
        FROM attendance a
        JOIN classschedule cs
            ON a.classCode = cs.classCode
        LEFT JOIN instructor i
            ON cs.instructID = i.instructID
        LEFT JOIN leaverequest lr
            ON cs.instructID = lr.instructID
            AND a.startDate BETWEEN lr.startDate AND lr.endDate
            AND lr.status = 'APPROVED'
        ORDER BY a.startDate DESC
        """;

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Attendance attendance = new Attendance(
                        rs.getInt("attendanceID"),
                        rs.getDate("startDate"),
                        rs.getDate("endDate"),
                        rs.getString("instructorStatus"),
                        rs.getString("remarks"),
                        rs.getString("classCode"),
                        rs.getInt("assignedInstructID"),
                        (Integer) rs.getObject("actualInstructID"),
                        (Integer) rs.getObject("leaveRequestID"),
                        rs.getObject("checkedBy") != null ? rs.getInt("checkedBy") : 0
                );

                list.add(attendance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // CHECKER DETAILS – CRUD
    // =========================================================================

    /** Return all checker-detail rows, joined with the checker's name. */
    public List<ref.CheckerDetail> getAllCheckerDetails() {
        List<ref.CheckerDetail> list = new ArrayList<>();
        String sql =
                "SELECT cd.*, su.name AS checkerName " +
                        "FROM checkerdetails cd " +
                        "JOIN systemuser su ON cd.checkerID = su.userID " +
                        "ORDER BY su.name, cd.day, cd.shiftStart";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ref.CheckerDetail cd = new ref.CheckerDetail(
                        rs.getInt("checkerID"),
                        rs.getInt("scheduleID"),
                        rs.getTime("shiftStart"),
                        rs.getTime("shiftEnd"),
                        rs.getString("building"),
                        rs.getString("floor"),
                        rs.getString("day")
                );
                cd.setCheckerName(rs.getString("checkerName"));
                list.add(cd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Insert a new checker-detail row. Returns true on success. */
    public boolean createCheckerDetail(ref.CheckerDetail cd) {
        String sql =
                "INSERT INTO checkerdetails (checkerID, scheduleID, shiftStart, shiftEnd, building, floor, day) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cd.getCheckerID());
            stmt.setInt(2, cd.getScheduleID());
            stmt.setTime(3, cd.getShiftStart());
            stmt.setTime(4, cd.getShiftEnd());
            stmt.setString(5, cd.getBuilding());
            stmt.setString(6, cd.getFloor());
            stmt.setString(7, cd.getDay());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Update an existing checker-detail row (identified by checkerID + scheduleID). */
    public boolean updateCheckerDetail(ref.CheckerDetail cd) {
        String sql =
                "UPDATE checkerdetails " +
                        "SET shiftStart = ?, shiftEnd = ?, building = ?, floor = ?, day = ? " +
                        "WHERE checkerID = ? AND scheduleID = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTime(1, cd.getShiftStart());
            stmt.setTime(2, cd.getShiftEnd());
            stmt.setString(3, cd.getBuilding());
            stmt.setString(4, cd.getFloor());
            stmt.setString(5, cd.getDay());
            stmt.setInt(6, cd.getCheckerID());
            stmt.setInt(7, cd.getScheduleID());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a checker-detail row (checkerID + scheduleID).
     * Also sets assignedChecker = NULL on any classschedule rows that referenced
     * this checker, so no foreign-key cascade breaks existing schedules.
     */
    public boolean deleteCheckerDetail(int checkerID, int scheduleID) {
        String nullifySchedules =
                "UPDATE classschedule SET assignedChecker = NULL WHERE assignedChecker = ?";
        String deleteRow =
                "DELETE FROM checkerdetails WHERE checkerID = ? AND scheduleID = ?";

        try (Connection conn = DataPB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Nullify class schedule assignments for this checker
                try (PreparedStatement s1 = conn.prepareStatement(nullifySchedules)) {
                    s1.setInt(1, checkerID);
                    s1.executeUpdate();
                }

                // 2. Delete the detail row
                try (PreparedStatement s2 = conn.prepareStatement(deleteRow)) {
                    s2.setInt(1, checkerID);
                    s2.setInt(2, scheduleID);
                    s2.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Convenience: get all system users who have the CHECKER role. */
    public List<SystemUser> getAllCheckerUsers() {
        List<SystemUser> list = new ArrayList<>();
        String sql = "SELECT * FROM systemuser WHERE role = 'CHECKER' ORDER BY name";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new SystemUser(
                        rs.getInt("userID"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getInt("createdBy")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Update Attendance methods

    /** Returns class schedules assigned to the given checker (assignedChecker = checkerID). */
    public List<ClassSchedule> getSchedulesByCheckerID(int checkerID) {
        List<ClassSchedule> list = new ArrayList<>();
        String sql = "SELECT s.*, i.name AS instructorName " +
                "FROM classschedule s " +
                "LEFT JOIN instructor i ON s.instructID = i.instructID " +
                "WHERE s.assignedChecker = ? " +
                "ORDER BY s.startTime";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, checkerID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule cs = new ClassSchedule(
                            rs.getString("classCode") /* classCode is VARCHAR */,
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("instructID"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedChecker")
                    );
                    cs.setInstructorName(rs.getString("instructorName"));
                    cs.setAssignedChecker(checkerID);
                    list.add(cs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Returns the existing attendance record for a class on a given date, or null if none. */
    public Attendance getAttendanceForClass(String classCode, java.sql.Date date) {
        String sql = """
        SELECT a.*, cs.instructID AS assignedInstructID
        FROM attendance a
        JOIN classschedule cs
            ON a.classCode = cs.classCode
        WHERE a.classCode = ?
          AND a.startDate = ?
        LIMIT 1
        """;

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, classCode);
            stmt.setDate(2, date);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Attendance(
                            rs.getInt("attendanceID"),
                            rs.getDate("startDate"),
                            rs.getDate("endDate"),
                            rs.getString("instructorStatus"),
                            rs.getString("remarks"),
                            rs.getString("classCode"),
                            rs.getInt("assignedInstructID"),
                            (Integer) rs.getObject("actualInstructID"),
                            (Integer) rs.getObject("leaveRequestID"),
                            rs.getObject("checkedBy") != null ? rs.getInt("checkedBy") : 0
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}