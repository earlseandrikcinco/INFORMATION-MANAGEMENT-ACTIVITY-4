package ref;

public class Instructor {
    private int instructorID;
    private String name;
    private int departmentID;
    private String departmentName;

    public Instructor(int instructorID, String name, int departmentID) {
        this.instructorID = instructorID;
        this.name = name;
        this.departmentID = departmentID;
    }

    public int getInstructorID(){
        return instructorID;
    }
    public String getName(){
        return name;
    }
    public int getDepartmentID(){return departmentID;}
    public String getDepartmentName() {return departmentName;}

    public void setInstructorID(int instructorID){
        this.instructorID = instructorID;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setDepartmentID(int departmentID){
        this.departmentID = departmentID;
    }
    public void setDepartmentName(String departmentName) {this.departmentName = departmentName;}
}
