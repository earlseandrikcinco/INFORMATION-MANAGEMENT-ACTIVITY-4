package ref;
import java.sql.Date;

public class Attendance {
    private int classCode;
    private Integer instructID;
    private Date date;
    private String instructorStatus;
    private int checkerID;
    private Integer leaveReqID;
    private boolean isSubstitute;

    public Attendance(int classCode, Integer instructID, Date date, String instructorStatus, int checkerID, Integer leaveReqID, boolean isSubstitute) {
        this.classCode = classCode;
        this.instructID = instructID;
        this.date = date;
        this.instructorStatus = instructorStatus;
        this.checkerID = checkerID;
        this.leaveReqID = leaveReqID;
        this.isSubstitute = isSubstitute;
    }

    public int getClassCode() {
        return classCode;
    }

    public void setClassCode(int classCode) {
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
}