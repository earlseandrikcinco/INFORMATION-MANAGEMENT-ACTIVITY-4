package ref;

public class Instructor {
    private int instructorID;
    private String name;
    private int departmentID;

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
    public int getDepartment(){
        return departmentID;
    }

    public void setInstructorID(int instructorID){
        this.instructorID = instructorID;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setDepartment(int departmentID){
        this.departmentID = departmentID;
    }
}
