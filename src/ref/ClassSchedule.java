package ref;

import java.sql.Time;
import java.time.LocalDateTime;

public class ClassSchedule {
    private int classCode;
    private String courseNo;
    private Time startTime;
    private Time endTime;
    private String days;
    private int instructID;
    private String room;

    public ClassSchedule(int classCode, String courseNo, Time startTime, Time endTime, String days, int instructID, String room){
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
