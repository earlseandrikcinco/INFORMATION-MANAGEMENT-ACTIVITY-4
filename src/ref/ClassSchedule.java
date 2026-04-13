package ref;

import java.sql.Time;

public class ClassSchedule {
    private int classCode;
    private String courseNo;
    private Time startTime;
    private Time endTime;
    private String days;
    private Integer roomID;
    private Integer instructID;
    private String instructorName;

    public ClassSchedule(int classCode, String courseNo, Time startTime, Time endTime, String days, Integer roomID, Integer instructID) {
        this.classCode = classCode;
        this.courseNo = courseNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.days = days;
        this.roomID = roomID;
        this.instructID = instructID;
    }

    public int getClassCode() {
        return classCode;
    }

    public void setClassCode(int classCode) {
        this.classCode = classCode;
    }

    public String getCourseNo() {
        return courseNo;
    }

    public void setCourseNo(String courseNo) {
        this.courseNo = courseNo;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public Integer getRoomID() {
        return roomID;
    }

    public Integer getInstructID() {
        return instructID;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setRoomID(Integer roomID) {
        this.roomID = roomID;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public void setInstructID(Integer instructID) {
        this.instructID = instructID;
    }
}