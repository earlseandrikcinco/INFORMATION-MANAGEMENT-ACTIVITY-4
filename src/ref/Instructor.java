package ref;

public class Instructor {
    private int instructorID;
    private String name;
    private String department;

    public Instructor(int instructorID, String name, String department){
        this.instructorID = instructorID;
        this.name = name;
        this.department = department;
    }

    public void setInstructorID(int instructorID){
        this.instructorID = instructorID;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setDepartment(String department){
        this.department = department;
    }

    public int getInstructorID(){
        return instructorID;
    }
    public String getName(){
        return name;
    }
    public String getDepartment(){
        return department;
    }

    @Override
    public String toString(){
        return "Instructor ID: " + instructorID + "\nName: " + name + "\nDepartment: " + department;
    }
}
