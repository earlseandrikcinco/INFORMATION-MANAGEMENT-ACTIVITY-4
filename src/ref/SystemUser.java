package ref;

import java.util.List;

public class SystemUser {
    private int userID;
    private String name;
    private String username;
    private String email;
    private String password;
    private String role;
    private int createdBy;  // TODO Check if this really should be non nullable
    private Integer departmentID;

    private String approvalCode;    // For admin
    private List<CheckerDetail> checkerDetails;   // For checker

    public SystemUser (int userID,
                       String name,
                       String username,
                       String email,
                       String password,
                       String role,
                       Integer createdBy) {
        this.userID = userID;
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdBy = createdBy;
    }

    public int getUserID(){
        return userID;
    }
    public String getName() {
        return name;
    }
    public String getUsername(){
        return username;
    }
    public String getPassword(){
        return password;
    }
    public String getRole(){
        return role;
    }
    public String getEmail(){
        return email;
    }
    public int getDepartmentID() {return departmentID;}
    public Integer getCreatedBy() {return createdBy;}

    public List<CheckerDetail> getCheckerDetails() {
        return "Checker".equalsIgnoreCase(this.role) ? this.checkerDetails : null;
    }
    public String getApprovalCode() {
        return "Admin".equalsIgnoreCase(this.role) ? this.approvalCode : null;
    }

    public void setUserID(int userID) {this.userID = userID;}
    public void setName(String name) {this.name = name;}
    public void setUsername(String username) {this.username = username;}
    public void setPassword(String password) {this.password = password;}
    public void setRole(String role) {this.role = role;}
    public void setEmail(String email) {this.email = email;}
    public void setCreatedBy(Integer createdBy) {this.createdBy = createdBy;}
    public void setCheckerDetails(List<CheckerDetail> checkerDetails) {this.checkerDetails = checkerDetails;}
    public void setDepartmentID(int departmentID) {this.departmentID = departmentID;}
    public void setApprovalCode(String approvalCode) {this.approvalCode = approvalCode;}

    @Override
    public String toString(){
        return "User ID: " + userID + "\nUsername: " + username + "\nRole: " + role + "\nEmail: " + email;
    }
}
