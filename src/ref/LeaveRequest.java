package ref;

import java.sql.Date;

public class LeaveRequest {
    private int leaveRegID;
    private int instructID;
    private String leaveType;
    private Date startDate;
    private Date endDate;
    private String status;
    private int filedBy;

    public LeaveRequest(int leaveReqID, int instructID, String leaveType, Date startDate, Date endDate, String status, int filedBy) {
        this.leaveRegID = leaveReqID;
        this.instructID = instructID;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.filedBy = filedBy;

    }

    public int getLeaveRegID() {
        return leaveRegID;
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
    public int getFiledBy() {
        return filedBy;
    }

    public void setLeaveRegID(int leaveRegID) {
        this.leaveRegID = leaveRegID;
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

    public void setFiledBy(int filedBy) {
        this.filedBy = filedBy;
    }
}
