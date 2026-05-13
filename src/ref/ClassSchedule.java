package ref;

import java.sql.Time;

public class ClassSchedule {

    private String classCode;
    private String courseNo;
    private Time startTime;
    private Time endTime;
    private String days;
    private Integer instructID;
    private Integer roomID;
    private Integer assignedChecker; // FK → checker.checkerID

    // Resolved display names (not stored in DB)
    private String instructorName;
    private String checkerName;
    private String roomDescription;

    public ClassSchedule(
            String classCode,
            String courseNo,
            Time startTime,
            Time endTime,
            String days,
            Integer instructID,
            Integer roomID,
            Integer assignedChecker
    ) {
        this.classCode = classCode;
        this.courseNo = courseNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.days = days;
        this.instructID = instructID;
        this.roomID = roomID;
        this.assignedChecker = assignedChecker;
    }

    // ── Getters ─────────────────────────────────────────

    public String getClassCode() {
        return classCode;
    }

    public String getCourseNo() {
        return courseNo;
    }

    public Time getStartTime() {
        return startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public String getDays() {
        return days;
    }

    public Integer getRoomID() {
        return roomID;
    }

    public Integer getInstructID() {
        return instructID;
    }

    public Integer getAssignedChecker() {
        return assignedChecker;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public String getCheckerName() {
        return checkerName;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    // ── Setters ─────────────────────────────────────────

    public void setClassCode(String classCode) { // Changed int to String
        this.classCode = classCode;
    }

    public void setCourseNo(String courseNo) {
        this.courseNo = courseNo;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public void setRoomID(Integer roomID) {
        this.roomID = roomID;
    }

    public void setInstructID(Integer instructID) {
        this.instructID = instructID;
    }

    public void setAssignedChecker(Integer checker) {
        this.assignedChecker = checker;
    }

    public void setInstructorName(String name) {
        this.instructorName = name;
    }

    public void setCheckerName(String name) {
        this.checkerName = name;
    }

    public void setRoomDescription(String desc) {
        this.roomDescription = desc;
    }
}