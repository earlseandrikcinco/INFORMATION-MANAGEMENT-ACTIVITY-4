import ref.SystemUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DataAccess {
    public SystemUser getUserLogin(String key){
        SystemUser user = null;

        try {

            Connection conn = DataPB.setConnection();

            String sql = "SELECT * FROM users WHERE username = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, key);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userID = rs.getInt("id");
                String name = rs.getString("name");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");
                String email = rs.getString("email");

                user = new SystemUser(userID, name, username, password, role, email);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;

    }
}
