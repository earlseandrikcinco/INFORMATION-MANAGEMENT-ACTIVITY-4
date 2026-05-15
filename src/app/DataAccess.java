package app;

import ref.*;

import java.sql.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("CallToPrintStackTrace")
public class DataAccess {
    public SystemUser getUser(String username) {
        try (Connection conn = DataPB.getConnection();
             CallableStatement stmt = conn.prepareCall("{call sp_GetSystemUser(?)}")) {

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
                    user.setDepartmentID(rs.getObject("departmentID") != null ? rs.getInt("departmentID") : null);
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



    public int getUnexcusedAbsenceCount(int instructorID) {
        int count = 0;
        // Logic:
        // 1. Instructor was assigned to the class AND no sub was present AND status is 'A' (and no leave).
        // 2. Instructor WAS the substitute (actualInstructorID) AND status is 'A'.
        String sql = """
        SELECT COUNT(*) FROM attendance a
        LEFT JOIN classschedule cs ON a.classCode = cs.classCode
        WHERE 
            (cs.instructID = ? AND a.actualInstructID IS NULL AND a.instructorStatus = 'A' AND a.leaveRequestID IS NULL)
            OR 
            (a.actualInstructID = ? AND a.instructorStatus = 'A')
    """;

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructorID);
            stmt.setInt(2, instructorID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }



    public Room getRoomByID(Integer roomID) {

        if (roomID == null) return null;

        String sql = """
        SELECT *
        FROM room
        WHERE roomID = ?
    """;

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomID);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                return new Room(
                        rs.getInt("roomID"),
                        rs.getString("floor"),
                        rs.getString("building"),
                        rs.getInt("capacity"),
                        rs.getString("roomType")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
        String sql = "CALL sp_GetAllClassSchedules";

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
    WHERE cs.instructID = ? 
      AND (a.instructorStatus = 'Absent' OR a.instructorStatus = 'A')
      AND (a.leaveRequestID IS NULL OR a.leaveRequestID = 0)
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

    public void syncLeaveToAttendance(LeaveRequest lr) {
        // This query updates any existing 'Absent' records to the excused status
        // and links the leaveRequestID to clear it from "Unexcused" counts.
        String sql = """
        UPDATE attendance a
        JOIN classschedule cs ON a.classCode = cs.classCode
        SET a.instructorStatus = ?, 
            a.leaveRequestID = ?
        WHERE cs.instructID = ? 
          AND a.date BETWEEN ? AND ?
          AND (a.instructorStatus = 'A' OR a.instructorStatus = 'Absent')
    """;

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, lr.getLeaveType());
            stmt.setInt(2, lr.getLeaveRequestID());
            stmt.setInt(3, lr.getInstructID());
            stmt.setDate(4, lr.getStartDate());
            stmt.setDate(5, lr.getEndDate());

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Sync complete: Updated " + rowsAffected + " attendance records.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public LeaveRequest getApprovedLeaveForDate(int instructorID, java.sql.Date date) {
        String sql = """
        SELECT * FROM leaverequest 
        WHERE instructID = ? AND status = 'Approved' 
        AND ? BETWEEN startDate AND endDate LIMIT 1
    """;
        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instructorID);
            stmt.setDate(2, date);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new LeaveRequest(
                            rs.getInt("leaveRequestID"), rs.getString("leaveType"),
                            rs.getDate("startDate"), rs.getDate("endDate"),
                            rs.getString("status"), rs.getString("leaveReason"),
                            rs.getInt("instructID"), rs.getInt("approvedBy")
                    );
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Integer getMatchingLeaveRequestID(int instructID, Date date, String statusAttempted) {
        // We only need to check the DB if the status is an "Excused" type
        if (statusAttempted.equals("Present") || statusAttempted.equals("Absent") ||
                statusAttempted.equals("P") || statusAttempted.equals("A")) {
            return null;
        }



        String sql = """
        SELECT leaveRequestID FROM leaverequest 
        WHERE instructID = ? 
          AND status = 'Approved' 
          AND leaveType = ? 
          AND ? BETWEEN startDate AND endDate
        LIMIT 1
    """;

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructID);
            stmt.setString(2, statusAttempted); // e.g., "Sick Leave"
            stmt.setDate(3, date);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("leaveRequestID");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
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

    public String getActualInstructorName(int actualID) {
        String name = "Unknown";
        String sql = "SELECT name FROM instructor WHERE instructID = ?";
        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, actualID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return name;
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
                if (!dayRegex.isEmpty()) dayRegex.append("|");
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
        if (dayRegex.isEmpty()) {
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

    public boolean hasScheduleConflict(String classCode, Integer roomID, Integer instructID, String days, Time start, Time end) {
        // We use SELECT instead of { ? = CALL } to avoid driver parameter-count bugs
        String sql = "SELECT fn_CheckScheduleConflict(?, ?, ?, ?, ?, ?) AS conflict";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Index 1-6 match the 6 parameters in the function
            stmt.setString(1, classCode);
            if (roomID != null) stmt.setInt(2, roomID); else stmt.setNull(2, Types.INTEGER);
            if (instructID != null) stmt.setInt(3, instructID); else stmt.setNull(3, Types.INTEGER);
            stmt.setString(4, days);
            stmt.setTime(5, start);
            stmt.setTime(6, end);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // If it returns 1, there is a conflict (true). If 0, no conflict (false).
                    return rs.getInt("conflict") == 1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Default to true (safe mode) if something fails
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

    public List<Attendance> getAttendanceRecords() {
        List<Attendance> list = new ArrayList<>();

        String sql = """
    SELECT a.*,
           cs.instructID AS assignedInstructID,
           i1.name AS instrName,
           i2.name AS actualInstrName,
           u.name AS checkerName
    FROM attendance a
    JOIN classschedule cs ON a.classCode = cs.classCode
    LEFT JOIN instructor i1 ON cs.instructID = i1.instructID
    LEFT JOIN instructor i2 ON a.actualInstructID = i2.instructID
    LEFT JOIN systemuser u ON a.checkedBy = u.userID
    ORDER BY a.startDate DESC
    """;

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Attendance a = new Attendance(
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

                // Map the names from the SQL result to the object
                a.setInstructorName(rs.getString("instrName"));
                a.setActualInstructorName(rs.getString("actualInstrName"));
                a.setCheckerName(rs.getString("checkerName"));

                list.add(a);
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

    /** BUG 4 – Returns the next auto-incremented scheduleID for a checker's detail records. */
    public int getNextCheckerScheduleID() {
        String sql = "SELECT COALESCE(MAX(scheduleID), 0) + 1 FROM checkerdetails";
        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /** BUG 3 – After a new checker detail is created, automatically assign this checker
     * to every class schedule whose room is in the same building+floor AND whose days
     * overlap the checker's day(s) AND whose time overlaps the checker's shift window.
     *
     * Day overlap: the checker's day string is treated as a set of MWF-style tokens;
     * a schedule day token matches if it appears anywhere in the checker's day string.
     *
     * @return number of class schedules updated
     */
    public int autoAssignCheckerToSchedules(ref.CheckerDetail cd) {
        // Find all class schedules in the matching building+floor whose time overlaps
        // the shift AND whose days overlap the checker day — only update if currently unassigned.
        String sql =
                "UPDATE classschedule cs " +
                        "JOIN room r ON cs.roomID = r.roomID " +
                        "SET cs.assignedChecker = ? " +
                        "WHERE r.building = ? " +
                        "  AND r.floor    = ? " +
                        "  AND cs.startTime < ? " +     // schedule starts before shift ends
                        "  AND cs.endTime   > ? " +     // schedule ends after shift starts
                        "  AND cs.assignedChecker IS NULL " +
                        "  AND EXISTS (" +
                        "      SELECT 1 FROM (" +
                        "          SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(?, ',', n.n), ',', -1)) AS tok" +
                        "          FROM (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4" +
                        "                UNION SELECT 5 UNION SELECT 6 UNION SELECT 7) n" +
                        "          WHERE n.n <= 1 + LENGTH(?) - LENGTH(REPLACE(?, ',', ''))" +
                        "      ) tokens " +
                        "      WHERE FIND_IN_SET(tokens.tok, cs.days) > 0 OR cs.days LIKE CONCAT('%', tokens.tok, '%')" +
                        "  )";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1,    cd.getCheckerID());
            stmt.setString(2, cd.getBuilding());
            stmt.setString(3, cd.getFloor());
            stmt.setTime(4,   cd.getShiftEnd());    // cs.startTime < shiftEnd
            stmt.setTime(5,   cd.getShiftStart());  // cs.endTime   > shiftStart
            stmt.setString(6, cd.getDay());
            stmt.setString(7, cd.getDay());
            stmt.setString(8, cd.getDay());

            return stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
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

    public Attendance getAttendanceForClass(String classCode, java.sql.Date date) {
        try (Connection conn = DataPB.getConnection();
             CallableStatement stmt = conn.prepareCall("{call sp_GetAttendanceByClass(?, ?)}")) {

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