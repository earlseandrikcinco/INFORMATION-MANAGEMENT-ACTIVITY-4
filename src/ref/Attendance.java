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
    private Integer leaveReqID;
    private int checkedBy;

    public Attendance(String attendanceID,
                      Date startDate, Date endDate,
                      String instructorStatus,
                      String remarks,
                      String classCode,
                      Integer assignedInstructID,
                      Integer actualInstructID,
                      Integer leaveReqID,
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
        this.leaveReqID = leaveReqID;
        this.checkedBy = checkedBy;
    }

    public String getAttendanceID() {return attendanceID;}

    public void setAttendanceID(String attendanceID) {this.attendanceID = attendanceID;}

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public int getAssignedInstructID() {
        return assignedInstructID;
    }

    public void setAssignedInstructID(int assignedInstructID) {
        this.assignedInstructID = assignedInstructID;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getInstructorStatus() {
        return instructorStatus;
    }

    public void setInstructorStatus(String instructorStatus) {
        this.instructorStatus = instructorStatus;
    }

    public int getCheckedBy() {
        return checkedBy;
    }

    public void setCheckedBy(int checkedBy) {
        this.checkedBy = checkedBy;
    }

    public Integer getLeaveReqID() {
        return leaveReqID;
    }

    public void setLeaveReqID(Integer leaveReqID) {
        this.leaveReqID = leaveReqID;
    }
}