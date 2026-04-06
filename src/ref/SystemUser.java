package ref;

public class SystemUser {
    private int userID;
    private String name;
    private String userName;
    private String password;
    private String role;
    private String email;

    public SystemUser() {}

    public SystemUser(int userID, String name, String userName, String password, String role, String email) {
        this.userID = userID;
        this.name = name;
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.email = email;
    }

    public int getUserID(){
        return userID;
    }
    public String getName() {
        return name;
    }
    public String getUserName(){
        return userName;
    }
    public String getPassword(){
        return password;
    }
    public String getRole(){
        return role;
    }
    public String getEmail(){
        return email;
    }

    @Override
    public String toString(){
        return "";
    }
}
