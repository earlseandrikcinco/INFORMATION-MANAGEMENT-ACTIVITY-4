package ref;

public class Room {
    private int roomID;
    private String floor;
    private String building;
    private int capacity;
    private String roomType;

    public Room(int roomID,
                String floor,
                String building,
                int capacity,
                String roomType) {
        this.roomID = roomID;
        this.floor = floor;
        this.building = building;
        this.capacity = capacity;
        this.roomType = roomType;
    }

    public int getRoomID() { return roomID; }
    public String getBuilding() { return building; }
    public String getFloor() { return floor; }
    public int getCapacity() {return capacity;}
    public String getRoomType() {return roomType;}

    public void setRoomID(int roomID) {this.roomID = roomID;}
    public void setBuilding(String building) {this.building = building;}
    public void setFloor(String floor) {this.floor = floor;}
    public void setCapacity(int capacity) {this.capacity = capacity;}
    public void setRoomType(String roomType) {this.roomType = roomType;}
    @Override
    public String toString() {
        return "Room " + roomID + " - " + building + " (F" + floor + ")";
    }
}