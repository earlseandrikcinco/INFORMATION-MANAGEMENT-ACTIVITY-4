package ref;

import java.util.Date;

public class Approval {
    private int leaveRequestID;
    private int userID;
    private Date approvalDate;
    private int sequenceNumber;
    private String status;

    public Approval(int leaveRequestID,
                    int userID,
                    Date approvalDate,
                    int sequenceNumber,
                    String status) {

        this.leaveRequestID = leaveRequestID;
        this.userID = userID;
        this.approvalDate = approvalDate;
        this.sequenceNumber = sequenceNumber;
        this.status = status;
    }

    public int getLeaveRequestID() {
        return leaveRequestID;
    }

    public int getUserID() {
        return userID;
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setLeaveRequestID(int leaveRequestID) {
        this.leaveRequestID = leaveRequestID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
