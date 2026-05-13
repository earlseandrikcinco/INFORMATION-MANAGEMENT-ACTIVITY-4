package ref;
import java.sql.Date;

public class Attendance {
    private String attendanceID;
    private Date startDate;
    private Date endDate;
    private String instructorStatus;
    private String remarks;
    private String classCode;
    private int assignedInstructID; // From ClassSchedule
    private Integer actualInstructID;   // From  Instructor (for substitutes)
    private Integer leaveRequestID;
    private int checkedBy;

    public Attendance(String attendanceID,
                      Date startDate, Date endDate,
                      String instructorStatus,
                      String remarks,
                      String classCode,
                      Integer assignedInstructID,
                      Integer actualInstructID,
                      Integer leaveRequestID,
                      int checkedBy
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

    public String getAttendanceID() {return attendanceID;}

    public void setAttendanceID(String attendanceID) {this.attendanceID = attendanceID;}

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

    public void setCheckedBy(int checkedBy) {
        this.checkedBy = checkedBy;
    }

    public Integer getLeaveRequestID() {
        return leaveRequestID;
    }

    public void setLeaveRequestID(Integer leaveRequestID) {
        this.leaveRequestID = leaveRequestID;
    }
}