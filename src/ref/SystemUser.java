package ref;

public class SystemUser {
    private int userID;
    private String name;
    private String username;
    private String email;
    private String password;
    private String role;
    private Integer createdBy; // Uses Integer to allow null

    public SystemUser (int userID, String name, String username, String email, String password, String role, Integer createdBy) {
        this.userID = userID;
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdBy = createdBy;
    }

    public int getUserID(){
        return userID;
    }
    public String getName() {
        return name;
    }
    public String getUsername(){
        return username;
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
    public Integer getCreatedBy() {return createdBy;}

    public void setUserID(int userID) {this.userID = userID;}
    public void setName(String name) {this.name = name;}
    public void setUsername(String username) {this.username = username;}
    public void setPassword(String password) {this.password = password;}
    public void setRole(String role) {this.role = role;}
    public void setEmail(String email) {this.email = email;}
    public void setCreatedBy(Integer createdBy) {this.createdBy = createdBy;}

    @Override
    public String toString(){
        return "User ID: " + userID + "\nUsername: " + username + "\nRole: " + role + "\nEmail: " + email;
    }
}
