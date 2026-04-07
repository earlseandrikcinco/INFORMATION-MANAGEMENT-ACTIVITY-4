package ref;

import jdk.jfr.DataAmount;

import java.time.LocalDateTime;
//import java.util.Date;
import java.sql.Date;

public class Attendance {
    private int attendID;
    private Date date;
    private String status;
    private String absenceReason;
    private boolean isAsynchronous;
    private int classCode;
    private int checkerID;
    private int leaveReqID;
    private int leaveInstructorID;
    private int substituteID;
    private Instructor instructor;
    private Instructor substitute;
    private ClassSchedule classSchedule;

    public Attendance(int attendID, Date date, String status, String absenceReason, boolean isAsynchronous, int classCode,
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
        this.instructor = null;
        this.classSchedule = null;
        this.substitute = null;
    }

    public int getAttendID() {
        return attendID;
    }

    public void setAttendID(int attendID) {
        this.attendID = attendID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public ClassSchedule getClassSchedule() {
        return classSchedule;
    }

    public void setClassSchedule(ClassSchedule classSchedule) {
        this.classSchedule = classSchedule;
    }

    public Instructor getSubstitute() {
        return substitute;
    }

    public void setSubstitute(Instructor substitute) {
        this.substitute = substitute;
    }

    @Override
    public String toString() {
        String async = isAsynchronous ? "Yes" : "No";
        String reason = (absenceReason != null && !absenceReason.isEmpty()) ? absenceReason : "N/A";
        String substituteName = substituteID != 0 ? "N/A" : substitute.getName();
        return String.format(
                "Attendance ID   : %d%n" +
                "Date            : %s%n" +
                "Status          : %s%n" +
                "Absence Reason  : %s%n" +
                "Asynchronous    : %s%n" +
                "Class Code      : %d" +
                "Substitute      : %s",
                attendID, date, status, reason, async, classCode, substituteName
        );
    }
}
