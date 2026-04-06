package ref;

import java.time.LocalDateTime;

public class Attendance {
    private int attendID;
    private String date;
    private String status;
    private String absenceReason;
    private boolean isAsynchronous;
    private int classCode;
    private int checkerID;
    private int leaveReqID;
    private int leaveInstructorID;
    private int substituteID;

    private Attendance(int attendID, String date, String status, String absenceReason, boolean isAsynchronous, int classCode,
                       int checkerID, int leaveReqID, int leaveInstructorID, int substituteID){
        this.attendID = attendID;
        this.date = date;
        this.status = status;
        this.absenceReason = absenceReason;
        this.isAsynchronous = isAsynchronous;
        this.classCode = classCode;
        this.checkerID = checkerID;
        this.leaveReqID = leaveReqID;
        this.leaveInstructorID = leaveInstructorID;
        this.substituteID = substituteID;
    }

    public int getAttendID() {
        return attendID;
    }

    public void setAttendID(int attendID) {
        this.attendID = attendID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAbsenceReason() {
        return absenceReason;
    }

    public void setAbsenceReason(String absenceReason) {
        this.absenceReason = absenceReason;
    }

    public boolean isAsynchronous() {
        return isAsynchronous;
    }

    public void setAsynchronous(boolean asynchronous) {
        isAsynchronous = asynchronous;
    }

    public int getClassCode() {
        return classCode;
    }

    public void setClassCode(int classCode) {
        this.classCode = classCode;
    }

    public int getCheckerID() {
        return checkerID;
    }

    public void setCheckerID(int checkerID) {
        this.checkerID = checkerID;
    }

    public int getLeaveReqID() {
        return leaveReqID;
    }

    public void setLeaveReqID(int leaveReqID) {
        this.leaveReqID = leaveReqID;
    }

    public int getLeaveInstructorID() {
        return leaveInstructorID;
    }

    public void setLeaveInstructorID(int leaveInstructorID) {
        this.leaveInstructorID = leaveInstructorID;
    }

    public int getSubstituteID() {
        return substituteID;
    }

    public void setSubstituteID(int substituteID) {
        this.substituteID = substituteID;
    }
}
