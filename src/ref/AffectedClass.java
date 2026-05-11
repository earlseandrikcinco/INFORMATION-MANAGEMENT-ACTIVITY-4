package ref;

import java.sql.Date;
import java.sql.Time;

// DTO for one attendance row joined with its class schedule and room info
public class AffectedClass {
    private int attendanceID;
    private Date date;
    private String courseNo;
    private Time startTime;
    private Time endTime;
    private String building;
    private String floor;
    private String instructorStatus; // 'Absent' | 'Substituted' | 'Asynchronous'
    private String substituteName;   // null if not yet assigned

    public AffectedClass(int attendanceID, Date date, String courseNo,
                         Time startTime, Time endTime,
                         String building, String floor,
                         String instructorStatus, String substituteName) {
        this.attendanceID = attendanceID;
        this.date = date;
        this.courseNo = courseNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.building = building;
        this.floor = floor;
        this.instructorStatus = instructorStatus;
        this.substituteName  = substituteName;
    }

    public int getAttendanceID(){ return attendanceID; }
    public Date getDate(){ return date; }
    public String getCourseNo(){ return courseNo; }
    public Time getStartTime(){ return startTime; }
    public Time getEndTime(){ return endTime; }
    public String getBuilding(){ return building; }
    public String getFloor(){ return floor; }
    public String getInstructorStatus(){ return instructorStatus; }
    public String getSubstituteName(){ return substituteName; }
}
