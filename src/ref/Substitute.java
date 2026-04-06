package ref;

public class Substitute {
    private int substituteID;
    private int attendID;
    private int substituteInstructID;
    private boolean isPlanned;

    public Substitute(int substituteID, int attendID, int substituteInstructID, boolean isPlanned) {
        this.substituteID = substituteID;
        this.attendID = attendID;
        this.substituteInstructID = substituteInstructID;
        this.isPlanned = isPlanned;
    }

    public int getSubstituteID() {
        return substituteID;
    }
    public int getAttendID() {
        return attendID;
    }
    public int getSubstituteInstructID() {
        return substituteInstructID;
    }
    public boolean getIsPlanned() {
        return isPlanned;
    }

    public void setSubstituteID(int substituteID) {
        this.substituteID = substituteID;
    }
    public void setAttendID(int attendID) {
        this.attendID = attendID;
    }
    public void setSubstituteInstructID(int substituteInstructID) {
        this.substituteInstructID = substituteInstructID;
    }
    public void setPlanned(boolean planned) {
        isPlanned = planned;
    }
}

