package ref;
import java.sql.Date;

public class Attendance {
    private int attendanceID;
    private Date startDate;
    private Date endDate;
    private String instructorStatus;
    private String remarks;
    private String classCode;
    private int assignedInstructID; // From ClassSchedule
    private Integer actualInstructID;   // From  Instructor (for substitutes)
    private Integer leaveRequestID;
    private Integer checkedBy;
    private String instructorName;
    private String actualInstructorName;
    private String checkerName;

    public Attendance(int attendanceID,
                      Date startDate, Date endDate,
                      String instructorStatus,
                      String remarks,
                      String classCode,
                      Integer assignedInstructID,   //TODO This is removed from physical schema?
                      Integer actualInstructID,
                      Integer leaveRequestID,
                      Integer checkedBy
                      ) {
        this.attendanceID = attendanceID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.instructorStatus = instructorStatus;
        this.remarks = remarks;
        this.classCode = classCode;
        this.assignedInstructID = assignedInstructID;
        this.actualInstructID = actualInstructID;
        this.leaveRequestID = leaveRequestID;
        this.checkedBy = checkedBy;
    }

    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }

    public String getActualInstructorName() { return actualInstructorName; }
    public void setActualInstructorName(String actualInstructorName) { this.actualInstructorName = actualInstructorName; }

    public String getCheckerName() { return checkerName; }
    public void setCheckerName(String checkerName) { this.checkerName = checkerName; }

    public int getAttendanceID() {return attendanceID;}

    public void setAttendanceID(int attendanceID) {this.attendanceID = attendanceID;}

    public int getAssignedInstructID() {
        return assignedInstructID;
    }

    public void setAssignedInstructID(int assignedInstructID) {
        this.assignedInstructID = assignedInstructID;
    }

    public Integer getActualInstructID() {return actualInstructID;}

    public void setActualInstructID(Integer actualInstructID) {this.actualInstructID = actualInstructID;}

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {return endDate;}

    public void setEndDate(Date endDate) {this.endDate = endDate;}

    public String getInstructorStatus() {
        return instructorStatus;
    }

    public void setInstructorStatus(String instructorStatus) {
        this.instructorStatus = instructorStatus;
    }

    public String getRemarks() {return remarks;}

    public void setRemarks(String remarks) {this.remarks = remarks;}

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public int getCheckedBy() {
        return checkedBy;
    }

    public void setCheckedBy(Integer checkedBy) {
        this.checkedBy = checkedBy;
    }

    public Integer getLeaveRequestID() {
        return leaveRequestID;
    }

    public void setLeaveRequestID(Integer leaveRequestID) {
        this.leaveRequestID = leaveRequestID;
    }
}