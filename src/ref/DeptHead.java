package ref;

public class DeptHead {
    private int userID;
    private String department;

    public DeptHead(int userID, String department){
        this.userID = userID;
        this.department = department;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
