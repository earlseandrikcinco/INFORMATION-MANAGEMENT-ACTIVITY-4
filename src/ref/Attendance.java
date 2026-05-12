package ref;
import java.sql.Date;

public class Attendance {
    private String classCode;
    private Integer instructID;
    private Date date;
    private String instructorStatus;
    private int checkerID;
    private Integer leaveReqID;
    private boolean isSubstitute;
    private String courseNo;
    private String instructorName;
    private String leaveType;
    private String leaveStatus;
    private String leaveReason;

    public Attendance(String classCode, Integer instructID, Date date, String instructorStatus, int checkerID, Integer leaveReqID, boolean isSubstitute) {
        this.classCode = classCode;
        this.instructID = instructID;
        this.date = date;
        this.instructorStatus = instructorStatus;
        this.checkerID = checkerID;
        this.leaveReqID = leaveReqID;
        this.isSubstitute = isSubstitute;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public int getInstructID() {
        return instructID;
    }

    public void setInstructID(int instructID) {
        this.instructID = instructID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getInstructorStatus() {
        return instructorStatus;
    }

    public void setInstructorStatus(String instructorStatus) {
        this.instructorStatus = instructorStatus;
    }

    public int getCheckerID() {
        return checkerID;
    }

    public void setCheckerID(int checkerID) {
        this.checkerID = checkerID;
    }

    public Integer getLeaveReqID() {
        return leaveReqID;
    }

    public void setLeaveReqID(Integer leaveReqID) {
        this.leaveReqID = leaveReqID;
    }

    public boolean isSubstitute() {
        return isSubstitute;
    }

    public void setSubstitute(boolean substitute) {
        isSubstitute = substitute;
    }

    public String getCourseNo() {
        return courseNo;
    }

    public void setCourseNo(String courseNo) {
        this.courseNo = courseNo;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getLeaveStatus() {
        return leaveStatus;
    }

    public void setLeaveStatus(String leaveStatus) {
        this.leaveStatus = leaveStatus;
    }

    public String getLeaveReason() {
        return leaveReason;
    }

    public void setLeaveReason(String leaveReason) {
        this.leaveReason = leaveReason;
    }
}