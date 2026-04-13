package app;

import ref.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataAccess {

    // DISPLAY

    public SystemUser getUser(String key) {
        String sql = "SELECT u.*, c.floor, d.departmentID AS deptHeadID, " +
                "s.departmentID AS secDeptID, a.approvalCode " +
                "FROM SYSTEM_USER u " +
                "LEFT JOIN CHECKER c ON u.userID = c.checkerID " +
                "LEFT JOIN DEPTHEAD d ON u.userID = d.deptheadID " +
                "LEFT JOIN SECRETARY s ON u.userID = s.secretaryID " +
                "LEFT JOIN ADMIN a ON u.userID = a.adminID " +
                "WHERE u.username = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    SystemUser baseUser = new SystemUser(
                            rs.getInt("userID"),
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("role"),
                            (Integer) rs.getObject("createdBy")
                    );

                    String role = baseUser.getRole();
                    return switch (role) {
                        case "Checker" -> new Checker(baseUser, rs.getInt("floor"));
                        case "DeptHead" -> new DeptHead(baseUser, rs.getInt("deptHeadID"));
                        case "Secretary" -> new Secretary(baseUser, rs.getInt("secDeptID"));
                        case "Admin" -> new Admin(baseUser, rs.getString("approvalCode"));
                        default -> baseUser;
                    };
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SystemUser getDeptHead(int deptID) {
        String sql = "SELECT u.username FROM SYSTEM_USER u " +
                "JOIN DEPTHEAD dh ON u.userID = dh.deptheadID " +
                "WHERE dh.departmentID = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deptID);
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

    public SystemUser getSecretary(int deptID) {
        String sql = "SELECT u.username FROM SYSTEM_USER u " +
                "JOIN SECRETARY s ON u.userID = s.secretaryID " +
                "WHERE s.departmentID = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deptID);
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

    public List<SystemUser> getCheckers() {
        List<SystemUser> checkers = new ArrayList<>();

        String sql = "SELECT username FROM SYSTEM_USER WHERE role = 'Checker' ORDER BY name";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
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

    public List<Department> getDepartments() {
        List<Department> depts = new ArrayList<>();
        String sql = "SELECT * FROM DEPARTMENT ORDER BY departmentName";

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
        String sql = "SELECT * FROM ROOM ORDER BY building, floor";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                rooms.add(new Room(
                        rs.getInt("roomID"),
                        rs.getString("building"),
                        rs.getInt("floor"),
                        rs.getInt("capacity"),
                        rs.getString("roomType")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public boolean setSubstitute(int classCode, Date date, int substituteID) {
        String sql = "UPDATE ATTENDANCE " +
                "SET instructID = ?, isSubstitute = true " +
                "WHERE classCode = ? AND date = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, substituteID);
            stmt.setInt(2, classCode);
            stmt.setDate(3, date);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ClassSchedule> getAllClassSchedules() {
        List<ClassSchedule> list = new ArrayList<>();
        String sql = "SELECT s.*, i.name AS instructorName " +
                "FROM CLASS_SCHEDULE s " +
                "LEFT JOIN INSTRUCTOR i ON s.instructID = i.instructID " +
                "ORDER BY s.startTime";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ClassSchedule cs = new ClassSchedule(
                        rs.getInt("classCode"),
                        rs.getString("courseNo"),
                        rs.getTime("startTime"),
                        rs.getTime("endTime"),
                        rs.getString("days"),
                        (Integer) rs.getObject("roomID"),
                        (Integer) rs.getObject("instructID")
                );
                cs.setInstructorName(rs.getString("instructorName"));
                list.add(cs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ClassSchedule> getAllClassSchedulesByDept(int deptID) {
        List<ClassSchedule> list = new ArrayList<>();

        String sql = "SELECT s.*, i.name AS instructorName " +
                "FROM CLASS_SCHEDULE s " +
                "JOIN INSTRUCTOR i ON s.instructID = i.instructID " +
                "WHERE i.departmentID = ? " +
                "ORDER BY s.startTime";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deptID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule cs = new ClassSchedule(
                            rs.getInt("classCode"),
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("instructID")
                    );
                    cs.setInstructorName(rs.getString("instructorName"));
                    list.add(cs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ClassSchedule> getSchedulesByDay(String dayCode) {
        List<ClassSchedule> list = new ArrayList<>();

        String sql = "SELECT s.*, i.name AS instructorName " +
                "FROM CLASSSCHEDULE s " +
                "LEFT JOIN INSTRUCTOR i ON s.instructID = i.instructID " +
                "WHERE s.days REGEXP ? " +
                "ORDER BY s.startTime";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // T and Th handling
            String regexPattern;
            if (dayCode.equals("T")) {
                regexPattern = "T([^h]|$)"; // T followed by anything except h, or end of string
            } else if (dayCode.equals("Th")) {
                regexPattern = "Th";
            } else {
                regexPattern = dayCode;
            }

            stmt.setString(1, regexPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule cs = new ClassSchedule(
                            rs.getInt("classCode"),
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("instructID")
                    );
                    cs.setInstructorName(rs.getString("instructorName"));
                    list.add(cs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ClassSchedule> getSchedulesByRoom(int roomID) {
        List<ClassSchedule> list = new ArrayList<>();
        String sql = "SELECT s.*, i.name AS instructorName " +
                "FROM CLASSSCHEDULE s " +
                "LEFT JOIN INSTRUCTOR i ON s.instructID = i.instructID " +
                "WHERE s.roomID = ? " +
                "ORDER BY FIELD(s.days, 'M', 'T', 'W', 'Th', 'F', 'S'), s.startTime";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule cs = new ClassSchedule(
                            rs.getInt("classCode"),
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("instructID")
                    );
                    cs.setInstructorName(rs.getString("instructorName"));
                    list.add(cs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ClassSchedule> getSchedulesByInstructor(int instructID) {
        List<ClassSchedule> list = new ArrayList<>();
        String sql = "SELECT s.*, r.building, r.floor " +
                "FROM CLASSSCHEDULE s " +
                "LEFT JOIN ROOM r ON s.roomID = r.roomID " +
                "WHERE s.instructID = ? " +
                "ORDER BY FIELD(s.days, 'M', 'T', 'W', 'Th', 'F', 'S'), s.startTime";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule cs = new ClassSchedule(
                            rs.getInt("classCode"),
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("instructID")
                    );
                    list.add(cs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ClassSchedule> getSchedulesByTimeRange(String dayCode, Time start, Time end) {
        List<ClassSchedule> list = new ArrayList<>();
        String sql = "SELECT s.*, i.name AS instructorName " +
                "FROM CLASSSCHEDULE s " +
                "LEFT JOIN INSTRUCTOR i ON s.instructID = i.instructID " +
                "WHERE s.days REGEXP ? AND s.startTime >= ? AND s.endTime <= ? " +
                "ORDER BY s.startTime";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dayCode);
            stmt.setTime(2, start);
            stmt.setTime(3, end);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule cs = new ClassSchedule(
                            rs.getInt("classCode"), rs.getString("courseNo"),
                            rs.getTime("startTime"), rs.getTime("endTime"),
                            rs.getString("days"), (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("instructID")
                    );
                    cs.setInstructorName(rs.getString("instructorName"));
                    list.add(cs);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<String> getInstructorNameList() {
        List<String> instructors = new ArrayList<>();
        String sql = "SELECT name FROM INSTRUCTOR ORDER BY name";

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
        String sql = "SELECT * FROM INSTRUCTOR WHERE name = ?";
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
        String sql = "SELECT * FROM ATTENDANCE " +
                "WHERE instructID = ? AND instructorStatus = 'Absent' " +
                "AND leaveReqNo IS NULL";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Attendance(
                            rs.getInt("classCode"),
                            rs.getInt("instructID"),
                            rs.getDate("date"),
                            rs.getString("instructorStatus"),
                            rs.getInt("checkerID"),
                            null, // leaveReqNo is null for unauthorized
                            rs.getBoolean("isSubstitute")
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
        String sql = "SELECT * FROM ATTENDANCE WHERE date = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Attendance(
                            rs.getInt("classCode"),
                            (Integer) rs.getObject("instructID"),
                            rs.getDate("date"),
                            rs.getString("instructorStatus"),
                            rs.getInt("checkerID"),
                            (Integer) rs.getObject("leaveReqNo"),
                            rs.getBoolean("isSubstitute")
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
        String sql = "SELECT * FROM SYSTEM_USER";

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
                "FROM leave_request lr " +
                "JOIN INSTRUCTOR i ON lr.instructID = i.instructID " +
                "WHERE lr.instructID = ? ORDER BY lr.startDate DESC";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructorID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LeaveRequest lr = new LeaveRequest(
                            rs.getInt("leaveReqNo"),
                            rs.getInt("instructID"),
                            rs.getString("leaveType"),
                            rs.getDate("startDate"),
                            rs.getDate("endDate"),
                            rs.getString("status"),
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

    public List<LeaveRequest> getLeaveRequestsByDept(int deptID) {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = "SELECT lr.*, i.name AS instructorName " +
                "FROM leave_request lr " +
                "JOIN INSTRUCTOR i ON lr.instructID = i.instructID " +
                "WHERE i.departmentID = ? " +
                "ORDER BY lr.startDate DESC";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deptID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LeaveRequest lr = new LeaveRequest(
                            rs.getInt("leaveReqNo"), rs.getInt("instructID"),
                            rs.getString("leaveType"), rs.getDate("startDate"),
                            rs.getDate("endDate"), rs.getString("status"),
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
                "FROM leave_request lr " +
                "JOIN INSTRUCTOR i ON lr.instructID = i.instructID " +
                "WHERE lr.status = ? AND i.departmentID = ? " +
                "ORDER BY lr.startDate DESC";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, deptID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LeaveRequest lr = new LeaveRequest(
                            rs.getInt("leaveReqNo"), rs.getInt("instructID"),
                            rs.getString("leaveType"), rs.getDate("startDate"),
                            rs.getDate("endDate"), rs.getString("status"),
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
                "FROM leave_request lr " +
                "JOIN INSTRUCTOR i ON lr.instructID = i.instructID " +
                "WHERE lr.instructID = ? AND i.departmentID = ? " +
                "ORDER BY lr.startDate DESC";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructorID);
            stmt.setInt(2, deptID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LeaveRequest lr = new LeaveRequest(
                            rs.getInt("leaveReqNo"),
                            rs.getInt("instructID"),
                            rs.getString("leaveType"),
                            rs.getDate("startDate"),
                            rs.getDate("endDate"),
                            rs.getString("status"),
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

    public List<Attendance> getAttendanceByInstructor(int instructorID) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE instructID = ?";

        try (Connection conn = DataPB.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, instructorID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Attendance(
                        rs.getInt("classCode"),
                        rs.getInt("instructID"),
                        rs.getDate("date"),
                        rs.getString("instructorStatus"),
                        rs.getInt("checkerID"),
                        rs.getInt("leaveReqNo"),
                        rs.getBoolean("isSubstitute")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ClassSchedule> getClassSchedules() {
        List<ClassSchedule> list = new ArrayList<>();
        String sql = "SELECT * FROM class_schedule";

        try (Connection conn = DataPB.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                list.add(new ClassSchedule(
                        rs.getInt("classCode"),
                        rs.getString("courseNo"),
                        rs.getTime("startTime"),
                        rs.getTime("endTime"),
                        rs.getString("days"),
                        rs.getInt("roomID"),
                        rs.getInt("instructID")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ClassSchedule> getClassSchedulesByInstructor(int instructorID) {
        List<ClassSchedule> list = new ArrayList<>();
        String sql = "SELECT * FROM class_schedule WHERE instructID = ?";

        try (Connection conn = DataPB.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, instructorID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new ClassSchedule(
                        rs.getInt("classCode"),
                        rs.getString("courseNo"),
                        rs.getTime("startTime"),
                        rs.getTime("endTime"),
                        rs.getString("days"),
                        rs.getInt("roomID"),
                        rs.getInt("instructID")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getPresentCount(int instructorID) {

        String sql = "SELECT COUNT(*) AS total FROM attendance " +
                "WHERE instructID = ? AND instructorStatus = 'Present'";

        try (Connection conn = DataPB.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, instructorID);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int getAbsenceCount(int instructorID) {

        String sql = "SELECT COUNT(*) AS total FROM attendance " +
                "WHERE instructID = ? AND instructorStatus = 'Absent'";

        try (Connection conn = DataPB.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, instructorID);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
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

    // CREATE

    public boolean addSystemUser(SystemUser user, Object extra) {
        String sqlUser = "INSERT INTO SYSTEM_USER (name, username, email, password, role, createdBy) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = DataPB.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmtUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            stmtUser.setString(1, user.getName());
            stmtUser.setString(2, user.getUsername());
            stmtUser.setString(3, user.getEmail());
            stmtUser.setString(4, user.getPassword());
            stmtUser.setString(5, user.getRole());
            stmtUser.setObject(6, user.getCreatedBy());

            int affectedRows = stmtUser.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating user failed.");

            ResultSet generatedKeys = stmtUser.getGeneratedKeys();
            if (!generatedKeys.next()) throw new SQLException("No ID obtained.");
            int newID = generatedKeys.getInt(1);

            String roleSql = switch (user.getRole()) {
                case "Checker" -> "INSERT INTO CHECKER (checkerID, floor) VALUES (?, ?)";
                case "Secretary" -> "INSERT INTO SECRETARY (secretaryID, departmentID) VALUES (?, ?)";
                case "DeptHead" -> "INSERT INTO DEPTHEAD (deptheadID, departmentID) VALUES (?, ?)";
                case "Admin" -> "INSERT INTO ADMIN (adminID, approvalCode) VALUES (?, ?)";
                default -> throw new SQLException("Invalid Role");
            };

            PreparedStatement stmtRole = conn.prepareStatement(roleSql);
            stmtRole.setInt(1, newID);
            stmtRole.setObject(2, extra); // Pass floor(int), deptID(int), or code(String)
            stmtRole.executeUpdate();

            conn.commit(); // Save everything
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public void logAttendance(Attendance att) {
        String sql = "INSERT INTO ATTENDANCE " +
                "(classCode, instructID, date, instructorStatus, checkerID, leaveReqNo, isSubstitute) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, att.getClassCode());
            stmt.setObject(2, att.getInstructID());
            stmt.setDate(3, att.getDate());
            stmt.setString(4, att.getInstructorStatus());
            stmt.setInt(5, att.getCheckerID());
            stmt.setObject(6, att.getLeaveReqID());
            stmt.setBoolean(7, att.isSubstitute());

            stmt.executeUpdate();
            System.out.println("Attendance logged successfully!");

        } catch (SQLException e) {
            System.err.println("Error logging attendance: " + e.getMessage());
        }
    }

    public boolean insertLeaveRequest(LeaveRequest lr) {
        String sql = "INSERT INTO LEAVE_REQUEST (instructID, leaveType, startDate, endDate, status, approvedBy) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, lr.getInstructID());
            stmt.setString(2, lr.getLeaveType());
            stmt.setDate(3, lr.getStartDate());
            stmt.setDate(4, lr.getEndDate());
            stmt.setString(5, lr.getStatus()); // Usually 'Pending' or 'Allowed'
            stmt.setObject(6, lr.getApprovedBy());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // UPDATE

    public boolean assignInstructorToClass(int classCode, int instructID) {
        String sql = "UPDATE CLASSSCHEDULE SET instructID = ? WHERE classCode = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructID);
            stmt.setInt(2, classCode);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resolveLeaveRequest(int leaveReqNo, String newStatus, int reviewerID) {
        String sql = "UPDATE LEAVE_REQUEST SET status = ?, approvedBy = ? WHERE leaveReqNo = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, reviewerID);
            stmt.setInt(3, leaveReqNo);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean linkAttendanceToLeave(int classCode, Date date, int leaveReqNo) {
        String sql = "UPDATE ATTENDANCE SET leaveReqNo = ? " +
                "WHERE classCode = ? AND date = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, leaveReqNo);
            stmt.setInt(2, classCode);
            stmt.setDate(3, date);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}