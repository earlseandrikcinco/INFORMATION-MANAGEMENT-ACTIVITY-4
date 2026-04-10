import ref.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataAccess {

    public SystemUser getUser(String key) {
        String sql = "SELECT u.*, c.floor, d.departmentID AS deptHeadID, " +
                "s.departmentID AS secDeptID, a.approvalCode " +
                "FROM SYSTEMUSER u " +
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

    public List<ClassSchedule> getAllClassSchedules() {
        List<ClassSchedule> list = new ArrayList<>();
        String sql = "SELECT s.*, i.name AS instructorName " +
                "FROM CLASSSCHEDULE s " +
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

    public List<String> getInstructorList() {
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

    public SystemUser getDeptHead(int deptID) {
        String sql = "SELECT u.username FROM SYSTEMUSER u " +
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
        String sql = "SELECT u.username FROM SYSTEMUSER u " +
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

        String sql = "SELECT username FROM SYSTEMUSER WHERE role = 'Checker' ORDER BY name";

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
}