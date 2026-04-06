package ref;

import java.time.LocalDateTime;

public class ClassSchedule {
    private int classCode;
    private String courseNo;
    private String startTime;
    private String endTime;
    private String days;
    private int instructID;
    private String room;

    public ClassSchedule(int classCode, String courseNo, String startTime, String endTime, String days, int instructID, String room){
        this.classCode = classCode;
        this.courseNo = courseNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.days = days;
        this.instructID = instructID;
        this.room = room;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public int getInstructID() {
        return instructID;
    }

    public void setInstructID(int instructID) {
        this.instructID = instructID;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}
