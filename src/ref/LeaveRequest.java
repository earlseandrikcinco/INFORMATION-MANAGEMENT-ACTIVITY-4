package ref;

import java.sql.Date;

public class LeaveRequest {
    private int leaveRequestID;
    private int instructID;
    private String leaveType;
    private Date startDate;
    private Date endDate;
    private String status;
    private Integer approvedBy;     //TODO Check in physical schema
    private String leaveReason;
    private String instructorName;  // For displaying purposes

    public LeaveRequest(int leaveRequestID, int instructID, String leaveType,
                        Date startDate, Date endDate, String status, Integer approvedBy) {
        this.leaveRequestID = leaveRequestID;
        this.instructID = instructID;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.approvedBy = approvedBy;
    }

    public int getLeaveRequestID() { return leaveRequestID; }
    public int getInstructID() { return instructID; }
    public String getLeaveType() { return leaveType; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public Integer getApprovedBy() { return approvedBy; }
    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
    public String getLeaveReason() { return leaveReason; }
    public void setLeaveReason(String leaveReason) { this.leaveReason = leaveReason; }
}