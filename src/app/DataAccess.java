package app;

import ref.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataAccess {
    public SystemUser getUser(String key) {
        String sql = "SELECT u.*, c.floor, d.departmentID AS deptHeadID, " +
                "s.departmentID AS secDeptID, a.approvalCode " +
                "FROM systemuser u " +
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
                        case "Checker" -> new Checker(baseUser, 0 /* floor not in checker table */);
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
        String sql = "SELECT u.username FROM systemuser u " +
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
        String sql = "SELECT u.username FROM systemuser u " +
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

        String sql = "SELECT username FROM systemuser WHERE role = 'CHECKER' ORDER BY name";

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
                        rs.getString("building"),
                        0 /* floor not in checker table */,
                        rs.getInt("capacity"),
                        rs.getString("roomType")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public boolean setSubstitute(String classCode, Date date, int substituteID) {
        String sql = "UPDATE attendance " +
                "SET instructID = ?, isSubstitute = true " +
                "WHERE classCode = ? AND startDate = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, substituteID);
            stmt.setString(2, classCode);
            stmt.setDate(3, date);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ClassSchedule> getAllClassSchedules() {
        List<ClassSchedule> list = new ArrayList<>();
        String sql = "SELECT s.*, i.name AS instructorName " +
                "FROM classschedule s " +
                "LEFT JOIN instructor i ON s.instructID = i.instructID " +
                "ORDER BY s.startTime";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ClassSchedule cs = new ClassSchedule(
                        rs.getString("classCode") /* classCode is VARCHAR */,
                        rs.getString("courseNo"),
                        rs.getTime("startTime"),
                        rs.getTime("endTime"),
                        rs.getString("days"),
                        (Integer) rs.getObject("roomID"),
                        (Integer) rs.getObject("assignedInstructID")
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

        String sql =
                "SELECT s.*, i.name AS instructorName " +
                        "FROM classschedule s " +
                        "LEFT JOIN instructor i ON s.instructID = i.instructID " +
                        "WHERE (i.departmentID = ? OR s.instructID IS NULL) " +
                        "ORDER BY s.startTime";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, deptID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule cs = new ClassSchedule(
                            rs.getString("classCode") /* classCode is VARCHAR */,
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedInstructID")
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
                "FROM classschedule s " +
                "LEFT JOIN instructor i ON s.instructID = i.instructID " +
                "WHERE s.days REGEXP ? " +
                "ORDER BY s.startTime";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String regexPattern;
            if (dayCode.equals("T")) {
                regexPattern = "T([^h]|$)";
            } else if (dayCode.equals("Th")) {
                regexPattern = "Th";
            } else {
                regexPattern = dayCode;
            }

            stmt.setString(1, regexPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule cs = new ClassSchedule(
                            rs.getString("classCode") /* classCode is VARCHAR */,
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedInstructID")
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
                "FROM classschedule s " +
                "LEFT JOIN instructor i ON s.instructID = i.instructID " +
                "WHERE s.roomID = ? " +
                "ORDER BY FIELD(s.days, 'M', 'T', 'W', 'Th', 'F', 'S'), s.startTime";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule cs = new ClassSchedule(
                            rs.getString("classCode") /* classCode is VARCHAR */,
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedInstructID")
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
                "FROM classschedule s " +
                "LEFT JOIN ROOM r ON s.roomID = r.roomID " +
                "WHERE s.instructID = ? " +
                "ORDER BY FIELD(s.days, 'M', 'T', 'W', 'Th', 'F', 'S'), s.startTime";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule cs = new ClassSchedule(
                            rs.getString("classCode") /* classCode is VARCHAR */,
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedInstructID")
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
                "FROM classschedule s " +
                "LEFT JOIN instructor i ON s.instructID = i.instructID " +
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
                            rs.getString("classCode") /* classCode is VARCHAR */, rs.getString("courseNo"),
                            rs.getTime("startTime"), rs.getTime("endTime"),
                            rs.getString("days"), (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedInstructID")
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
                            rs.getInt("assignedInstructID"),
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
        String sql = "SELECT * FROM attendance " +
                "WHERE assignedInstructID = ? AND instructorStatus = 'Absent' " +
                "AND leaveRequestID IS NULL";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, instructID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Attendance(
                            rs.getString("classCode") /* classCode is VARCHAR */,
                            rs.getInt("assignedInstructID"),
                            rs.getDate("startDate"),
                            rs.getString("instructorStatus"),
                            rs.getObject("checkedBy") != null ? rs.getInt("checkedBy") : 0,
                            null,
                            false /* isSubstitute not in schema */
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
        String sql = "SELECT * FROM attendance WHERE startDate = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, date);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Attendance(
                            rs.getString("classCode") /* classCode is VARCHAR */,
                            (Integer) rs.getObject("assignedInstructID"),
                            rs.getDate("startDate"),
                            rs.getString("instructorStatus"),
                            rs.getObject("checkedBy") != null ? rs.getInt("checkedBy") : 0,
                            (Integer) rs.getObject("leaveRequestID"),
                            false /* isSubstitute not in schema */
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
                            rs.getInt("assignedInstructID"),
                            rs.getString("leaveType"),
                            rs.getDate("startDate"),
                            rs.getDate("endDate"),
                            rs.getString("status"),
                            (Integer) null /* approvedBy not in schema */
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
                            rs.getInt("leaveRequestID"), rs.getInt("assignedInstructID"),
                            rs.getString("leaveType"), rs.getDate("startDate"),
                            rs.getDate("endDate"), rs.getString("status"),
                            (Integer) null /* approvedBy not in schema */
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
                            rs.getInt("leaveRequestID"), rs.getInt("assignedInstructID"),
                            rs.getString("leaveType"), rs.getDate("startDate"),
                            rs.getDate("endDate"), rs.getString("status"),
                            (Integer) null /* approvedBy not in schema */
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
                            rs.getInt("assignedInstructID"),
                            rs.getString("leaveType"),
                            rs.getDate("startDate"),
                            rs.getDate("endDate"),
                            rs.getString("status"),
                            0 /* approvedBy not in schema */
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
        String sql = "SELECT * FROM attendance WHERE assignedInstructID = ? " +
                "ORDER BY startDate";

        try (Connection conn = DataPB.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, instructorID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Attendance(
                        rs.getString("classCode") /* classCode is VARCHAR */,
                        rs.getInt("assignedInstructID"),
                        rs.getDate("startDate"),
                        rs.getString("instructorStatus"),
                        rs.getObject("checkedBy") != null ? rs.getInt("checkedBy") : 0,
                        (Integer) rs.getObject("leaveRequestID"),
                        false /* isSubstitute not in schema */
                ));
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
                        rs.getInt("roomID"),
                        rs.getInt("assignedInstructID")
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
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedInstructID")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getPresentCount(int instructorID) {

        String sql = "SELECT COUNT(*) AS total FROM attendance " +
                "WHERE assignedInstructID = ? AND instructorStatus = 'Present'";

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
                "WHERE assignedInstructID = ? AND instructorStatus = 'Absent'";

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
                            rs.getInt("assignedInstructID"),
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

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Instructor instructor = new Instructor(
                            rs.getInt("assignedInstructID"),
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
        List<ClassSchedule> conflicts = findScheduleConflicts(
                cs.getClassCode(), // Now correctly passing the String
                cs.getRoomID(),
                cs.getInstructID(),
                cs.getDays(),
                cs.getStartTime(),
                cs.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            if (conflictHolder != null) {
                conflictHolder.clear();
                conflictHolder.addAll(conflicts);
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

    public boolean addSystemUser(SystemUser user, Object extra, int adminID) {
        String sqlUser = "INSERT INTO systemuser (name, username, email, password, role, createdBy) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataPB.getConnection()) {
            conn.setAutoCommit(false);

            int newID;
            try (PreparedStatement stmtUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                stmtUser.setString(1, user.getName());
                stmtUser.setString(2, user.getUsername());
                stmtUser.setString(3, user.getEmail());
                stmtUser.setString(4, user.getPassword());
                stmtUser.setString(5, user.getRole());
                stmtUser.setObject(6, adminID);

                if (stmtUser.executeUpdate() == 0) throw new SQLException("User insert failed.");

                try (ResultSet generatedKeys = stmtUser.getGeneratedKeys()) {
                    if (!generatedKeys.next()) throw new SQLException("No ID obtained.");
                    newID = generatedKeys.getInt(1);
                }
            }

            String roleSql = switch (user.getRole()) {
                case "Checker"   -> "INSERT INTO checker (checkerID) VALUES (?)";
                case "Secretary" -> "INSERT INTO SECRETARY (secretaryID, departmentID) VALUES (?, ?)";
                case "DeptHead"  -> "INSERT INTO DEPTHEAD (deptheadID, departmentID) VALUES (?, ?)";
                case "Admin"     -> "INSERT INTO admin (adminID, approvalCode) VALUES (?, ?)";
                default          -> throw new SQLException("Invalid Role: " + user.getRole());
            };

            try (PreparedStatement stmtRole = conn.prepareStatement(roleSql)) {
                stmtRole.setInt(1, newID);
                if (!"Checker".equals(user.getRole())) {
                    stmtRole.setObject(2, extra);
                }
                stmtRole.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void logAttendance(Attendance att) {
        String sql = "INSERT INTO attendance " +
                "(classCode, assignedInstructID, startDate, endDate, instructorStatus, leaveRequestID, checkedBy) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, att.getClassCode());
            stmt.setObject(2, att.getInstructID());
            stmt.setDate(3, att.getDate());
            stmt.setDate(4, att.getDate()); // endDate same as startDate for single-day record
            stmt.setString(5, att.getInstructorStatus());
            stmt.setObject(6, att.getLeaveReqID());
            stmt.setObject(7, att.getCheckerID() != 0 ? att.getCheckerID() : null);

            stmt.executeUpdate();
            System.out.println("Attendance logged successfully!");

        } catch (SQLException e) {
            System.err.println("Error logging attendance: " + e.getMessage());
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


    public boolean updateLeaveStatus(int instructID, int reqID, String status, int adminID) {
        String sql = "UPDATE leaverequest SET status = ? WHERE instructID = ? AND leaveRequestID = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, instructID);
            stmt.setInt(3, reqID);

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

    public boolean resolveLeaveRequest(int instructID, int leaveRequestID, String newStatus, int reviewerID) {
        String sql = "UPDATE leaverequest SET status = ? WHERE instructID = ? AND leaveRequestID = ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, instructID);
            stmt.setInt(3, leaveRequestID);

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

    public void syncLeaveToAttendance(LeaveRequest leave) {
        List<ClassSchedule> schedules = getAllClassSchedulesByInstructor(leave.getInstructID());

        String sql = "INSERT INTO attendance (classCode, assignedInstructID, startDate, endDate, instructorStatus, leaveRequestID) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE instructorStatus = VALUES(instructorStatus), leaveRequestID = VALUES(leaveRequestID)";

        try (Connection conn = DataPB.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(leave.getStartDate());

                while (!cal.getTime().after(leave.getEndDate())) {
                    java.sql.Date currentDate = new java.sql.Date(cal.getTimeInMillis());
                    String currentDayLetter = getDayLetter(cal.get(java.util.Calendar.DAY_OF_WEEK));

                    for (ClassSchedule s : schedules) {
                        if (s.getDays().contains(currentDayLetter)) {
                            stmt.setString(1, s.getClassCode());
                            stmt.setInt(2, leave.getInstructID());
                            stmt.setDate(3, currentDate);
                            stmt.setDate(4, currentDate); // endDate same as startDate
                            stmt.setString(5, "Absent");
                            stmt.setInt(6, leave.getLeaveReqID());

                            stmt.addBatch();
                        }
                    }
                    cal.add(java.util.Calendar.DATE, 1);
                }
                stmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

    public List<ClassSchedule> findScheduleConflicts(
            String classCode, // Changed from int to String
            Integer roomID,
            Integer instructorID,
            String days,
            Time startTime,
            Time endTime
    ) {
        List<ClassSchedule> conflicts = new ArrayList<>();

        String sql = "SELECT s.*, i.name AS instructorName " +
                "FROM classschedule s " +
                "LEFT JOIN instructor i ON s.instructID = i.instructID " +
                "WHERE s.classCode <> ? " + // DB expects VARCHAR
                "AND ((s.roomID IS NOT NULL AND s.roomID = ?) " +
                "     OR (s.instructID IS NOT NULL AND s.instructID = ?)) " +
                "AND s.startTime < ? " +
                "AND s.endTime > ?";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, classCode); // Changed setInt to setString
            stmt.setObject(2, roomID);
            stmt.setObject(3, instructorID);
            stmt.setTime(4, endTime);
            stmt.setTime(5, startTime);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String existingDays = rs.getString("days");
                    if (daysOverlap(days, existingDays)) {
                        ClassSchedule cs = new ClassSchedule(
                                rs.getString("classCode"),
                                rs.getString("courseNo"),
                                rs.getTime("startTime"),
                                rs.getTime("endTime"),
                                existingDays,
                                (Integer) rs.getObject("roomID"),
                                (Integer) rs.getObject("assignedInstructID")
                        );
                        cs.setInstructorName(rs.getString("instructorName"));
                        conflicts.add(cs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conflicts;
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
                            rs.getString("classCode") /* classCode is VARCHAR */, // adjust if classCode becomes String
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedInstructID")
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
        String sql = "UPDATE CLASS_SCHEDULE SET instructID = ?, assignedChecker = ? WHERE classCode = ?";
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
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedInstructID")
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
                            rs.getInt("assignedInstructID"),
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
                   i.name AS instructorName,
                   lr.leaveRequestID,
                   lr.leaveType,
                   lr.status AS leaveStatus,
                   lr.leaveReason AS reason
            FROM attendance a
            JOIN classschedule cs
                ON a.classCode = cs.classCode
            LEFT JOIN instructor i
                ON a.assignedInstructID = i.instructID
            LEFT JOIN leaverequest lr
                ON a.assignedInstructID = lr.instructID
                AND a.startDate BETWEEN lr.startDate AND lr.endDate
                AND lr.status = 'APPROVED'
            ORDER BY a.startDate DESC
            """;

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Attendance attendance = new Attendance(
                        rs.getString("classCode") /* classCode is VARCHAR */,
                        rs.getObject("assignedInstructID") != null ? rs.getInt("assignedInstructID") : null,
                        rs.getDate("startDate"),
                        rs.getString("instructorStatus"),
                        rs.getObject("checkedBy") != null ? rs.getInt("checkedBy") : 0,
                        rs.getObject("leaveRequestID") != null ? rs.getInt("leaveRequestID") : null,
                        false /* isSubstitute not in schema */
                );

                attendance.setCourseNo(rs.getString("courseNo"));
                attendance.setInstructorName(rs.getString("instructorName"));
                attendance.setLeaveType(rs.getString("leaveType"));
                attendance.setLeaveStatus(rs.getString("leaveStatus"));
                attendance.setLeaveReason(rs.getString("leaveReason"));

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
                        rs.getObject("checkedBy") != null ? rs.getInt("checkedBy") : 0,
                        rs.getInt("scheduleID"),
                        rs.getString("shiftStart"),
                        rs.getString("shiftEnd"),
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
            stmt.setString(3, cd.getShiftStart());
            stmt.setString(4, cd.getShiftEnd());
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

            stmt.setString(1, cd.getShiftStart());
            stmt.setString(2, cd.getShiftEnd());
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
                            rs.getString("classCode"),
                            rs.getString("courseNo"),
                            rs.getTime("startTime"),
                            rs.getTime("endTime"),
                            rs.getString("days"),
                            (Integer) rs.getObject("roomID"),
                            (Integer) rs.getObject("assignedInstructID")
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
        String sql = "SELECT * FROM attendance WHERE classCode = ? AND startDate = ? LIMIT 1";

        try (Connection conn = DataPB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, classCode);
            stmt.setDate(2, date);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Attendance(
                            rs.getString("classCode"),
                            rs.getObject("assignedInstructID") != null ? rs.getInt("assignedInstructID") : null,
                            rs.getDate("startDate"),
                            rs.getString("instructorStatus"),
                            rs.getObject("checkedBy") != null ? rs.getInt("checkedBy") : 0,
                            rs.getObject("leaveRequestID") != null ? rs.getInt("leaveRequestID") : null,
                            false
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates the status of an existing attendance record Or inserts a new one
     * if no record exists for the given Class and date. Returns true on success.
     */
    public boolean upsertAttendance(String classCode, java.sql.Date date,
                                    String status, int checkerID, Integer instructID) {
        if (getAttendanceForClass(classCode, date) != null) {
            // UPDATE existing record
            String sql = "UPDATE attendance " +
                    "SET instructorStatus = ?, checkedBy = ? " +
                    "WHERE classCode = ? AND startDate = ?";

            try (Connection conn = DataPB.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, status);
                stmt.setInt(2, checkerID);
                stmt.setString(3, classCode);
                stmt.setDate(4, date);
                return stmt.executeUpdate() > 0;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            // INSERT new record
            String sql = "INSERT INTO attendance " +
                    "(classCode, assignedInstructID, startDate, endDate, instructorStatus, checkedBy) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = DataPB.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, classCode);
                if (instructID != null) stmt.setInt(2, instructID);
                else                    stmt.setNull(2, java.sql.Types.INTEGER);
                stmt.setDate(3, date);
                stmt.setDate(4, date); // endDate same as startDate for a single-day entry
                stmt.setString(5, status);
                stmt.setInt(6, checkerID);
                return stmt.executeUpdate() > 0;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}