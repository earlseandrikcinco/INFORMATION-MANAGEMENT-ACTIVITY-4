package ref;

public class Secretary extends SystemUser {
    private int departmentID;

    public Secretary(int userID, String name, String username, String email, String password, String role, Integer createdBy, int departmentID) {
        super(userID, name, username, email, password, role, createdBy);
        this.departmentID = departmentID;
    }

    public Secretary(SystemUser user, int departmentID) {
        super(user.getUserID(), user.getName(), user.getUsername(), user.getEmail(),
                user.getPassword(), user.getRole(), user.getCreatedBy());
        this.departmentID = departmentID;
    }

    public int getDepartmentID() { return departmentID; }
    public void setDepartmentID(int departmentID) { this.departmentID = departmentID; }
}