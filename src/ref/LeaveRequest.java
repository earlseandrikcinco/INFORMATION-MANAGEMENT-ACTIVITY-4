package ref;

import java.sql.Date;

public class LeaveRequest {
    private int leaveReqID;
    private int instructID;
    private String leaveType;
    private Date startDate;
    private Date endDate;
    private String status;
    private int approvedBy;
    private String instructorName;

    public LeaveRequest(int leaveReqID, int instructID, String leaveType,
                        Date startDate, Date endDate, String status, int approvedBy) {
        this.leaveReqID = leaveReqID;
        this.instructID = instructID;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.approvedBy = approvedBy;
    }

    public int getLeaveReqID() { return leaveReqID; }
    public int getInstructID() { return instructID; }
    public String getLeaveType() { return leaveType; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public int getApprovedBy() { return approvedBy; }
    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
}