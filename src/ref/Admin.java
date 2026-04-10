package ref;

public class Admin extends SystemUser {
    private String approvalCode;

    public Admin (int userID, String name, String username, String email, String password, String role, Integer createdBy, String approvalCode) {
        super(userID, name, username, email, password, role, createdBy);
        this.approvalCode = approvalCode;
    }

    public Admin(SystemUser user, String approvalCode) {
        super(user.getUserID(), user.getName(), user.getUsername(), user.getEmail(),
                user.getPassword(), user.getRole(), user.getCreatedBy());
        this.approvalCode = approvalCode;
    }

    public String getApprovalCode() { return approvalCode; }
    public void setApprovalCode(String approvalCode) { this.approvalCode = approvalCode; }
}