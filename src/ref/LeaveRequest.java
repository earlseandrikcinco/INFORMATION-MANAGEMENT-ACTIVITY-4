package ref;
import java.sql.Date;

public class LeaveRequest {
    private int leaveReqID;
    private int instructID;
    private String leaveType;
    private Date startDate;
    private Date endDate;
    private String status;
    private Integer approvedBy; // Foreign Key to SystemUser

    public LeaveRequest(int leaveReqID, int instructID, String leaveType, Date startDate, Date endDate, String status, Integer approvedBy) {
        this.leaveReqID = leaveReqID;
        this.instructID = instructID;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.approvedBy = approvedBy;
    }

    // Getters and Setters
    public int getLeaveReqID() {
        return leaveReqID;
    }
    public int getInstructID() {
        return instructID;
    }
    public String getLeaveType() {
        return leaveType;
    }
    public Date getStartDate() {
        return startDate;
    }
    public Date getEndDate() {
        return endDate;
    }
    public String getStatus() {
        return status;
    }
    public int getApprovedBy() {return approvedBy;}

    public void setLeaveReqID(int leaveRegID) {
        this.leaveReqID = leaveRegID;
    }

    public void setInstructID(int instructID) {
        this.instructID = instructID;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setApprovedBy(int approvedBy) {
        this.approvedBy = approvedBy;
    }
}
