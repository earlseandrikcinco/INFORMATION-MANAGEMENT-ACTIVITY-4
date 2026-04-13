package ref;

public class Department {
    private int departmentID;
    private String departmentName;
    private String school;

    public Department(int departmentID, String departmentName, String school) {
        this.departmentID = departmentID;
        this.departmentName = departmentName;
        this.school = school;
    }

    public int getDepartmentID() { return departmentID; }
    public String getDepartmentName() { return departmentName; }
    public String getSchool() { return school; }

    public void setDepartmentID(int departmentID) {this.departmentID = departmentID;}
    public void setDepartmentName(String departmentName) {this.departmentName = departmentName;}
    public void setSchool(String school) {this.school = school;}

    @Override
    public String toString() {
        return departmentName;
    }
}