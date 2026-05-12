package ref;

public class CheckerDetail {

    private int checkerID;
    private int scheduleID;
    private String shiftStart;   // stored as "HH:mm"
    private String shiftEnd;
    private String building;
    private String floor;
    private String day;

    // checker's name – populated via JOIN
    private String checkerName;

    public CheckerDetail(int checkerID, int scheduleID,
                         String shiftStart, String shiftEnd,
                         String building, String floor, String day) {
        this.checkerID   = checkerID;
        this.scheduleID  = scheduleID;
        this.shiftStart  = shiftStart;
        this.shiftEnd    = shiftEnd;
        this.building    = building;
        this.floor       = floor;
        this.day         = day;
    }

    // ── getters ──────────────────────────────────────────────────────────────
    public int    getCheckerID()   { return checkerID;   }
    public int    getScheduleID()  { return scheduleID;  }
    public String getShiftStart()  { return shiftStart;  }
    public String getShiftEnd()    { return shiftEnd;    }
    public String getBuilding()    { return building;    }
    public String getFloor()       { return floor;       }
    public String getDay()         { return day;         }
    public String getCheckerName() { return checkerName; }

    // ── setters ──────────────────────────────────────────────────────────────
    public void setCheckerID(int checkerID)     { this.checkerID  = checkerID;  }
    public void setScheduleID(int scheduleID)   { this.scheduleID = scheduleID; }
    public void setShiftStart(String s)         { this.shiftStart = s;          }
    public void setShiftEnd(String s)           { this.shiftEnd   = s;          }
    public void setBuilding(String b)           { this.building   = b;          }
    public void setFloor(String f)              { this.floor      = f;          }
    public void setDay(String d)                { this.day        = d;          }
    public void setCheckerName(String n)        { this.checkerName = n;         }

    @Override
    public String toString() {
        return day + " | " + shiftStart + "–" + shiftEnd
                + " | " + building + ", " + floor;
    }
}
