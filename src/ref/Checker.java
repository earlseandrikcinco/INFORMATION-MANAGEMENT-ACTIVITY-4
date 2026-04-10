package ref;

public class Checker extends SystemUser {
    private int floor;

    public Checker (int userID, String name, String username, String email, String password, String role, Integer createdBy, int floor) {
        super(userID, name, username, email, password, role, createdBy);
        this.floor = floor;
    }

    public Checker(SystemUser user, int floor) {
        super(user.getUserID(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.getCreatedBy());
        this.floor = floor;
    }
    public int getFloor() { return floor; }

    public void setFloor(int floor) {
        this.floor = floor;
    }
}