package dao;

import jakarta.enterprise.context.ApplicationScoped;
import model.User;
import util.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@ApplicationScoped
public class UserDAO {

    // Login method
    public User login(String username, String password) {
        if (username == null || password == null) return null;

        username = username.trim();
        password = password.trim();

        // Debug: show input credentials
        System.out.println("[Login Attempt] username=" + username + ", password=" + password);

        String sql = "SELECT * FROM users WHERE username=? AND password=?";

        try (Connection conn = DBConnectionManager.getConnection()) {

            // Debug: show which database we are connected to
            try (Statement stmtDb = conn.createStatement();
                 ResultSet rsDb = stmtDb.executeQuery("SELECT DATABASE()")) {
                if (rsDb.next()) {
                    System.out.println("[UserDAO] Connected to database: " + rsDb.getString(1));
                }
            }

            // Debug: list all users in the database
            System.out.println("[UserDAO] Listing all users in the DB:");
            try (Statement stmtAll = conn.createStatement();
                 ResultSet rsAll = stmtAll.executeQuery("SELECT id, username, password, role FROM users")) {
                while (rsAll.next()) {
                    System.out.println("DB User: " + rsAll.getString("username") +
                                       " / " + rsAll.getString("password") +
                                       " (Role: " + rsAll.getString("role") + ")");
                }
            }

            // Attempt login
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                System.out.println("[UserDAO] Executing login query for username: " + username);

                stmt.setString(1, username);
                stmt.setString(2, password);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role"));

                    System.out.println("[UserDAO] Login successful for: " + user.getUsername() +
                                       " (Role: " + user.getRole() + ")");
                    return user;
                } else {
                    System.out.println("[UserDAO] Login failed: no matching user found.");
                }
            }

        } catch (Exception e) {
            System.err.println("[UserDAO] Error in login: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
