package dao;

import jakarta.enterprise.context.ApplicationScoped;
import model.Admin;
import util.DBConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AdminDAO {

    // Create a new admin with username/password
    public boolean createAdmin(Admin admin, String username, String password) {
        if (admin == null || username == null || password == null) {
            return false;
        }

        String insertUserSQL = "INSERT INTO users (username, password, role) VALUES (?, ?, 'ADMIN')";
        String insertAdminSQL = "INSERT INTO admins (name, email, phone, user_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnectionManager.getConnection()) {
            conn.setAutoCommit(false);

            int userId;

            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSQL, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, username);
                userStmt.setString(2, password);
                userStmt.executeUpdate();

                try (ResultSet rs = userStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to obtain user ID.");
                    }
                }
            }

            try (PreparedStatement adminStmt = conn.prepareStatement(insertAdminSQL, Statement.RETURN_GENERATED_KEYS)) {
                adminStmt.setString(1, admin.getName());
                adminStmt.setString(2, admin.getEmail());
                adminStmt.setString(3, admin.getPhone());
                adminStmt.setInt(4, userId);
                adminStmt.executeUpdate();

                try (ResultSet rs = adminStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        admin.setId(rs.getInt(1));
                    }
                }
            }

            conn.commit();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check username / email existence
    public boolean usernameExists(String username) {
        String sql = "SELECT id FROM users WHERE username=?";
        try (Connection conn = DBConnectionManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean emailExists(String email) {
        String sql = "SELECT id FROM admins WHERE email=?";
        try (Connection conn = DBConnectionManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get one admin by ID (missing earlier)
    public Admin getAdminById(int id) {
        String sql = "SELECT a.id, a.name, a.email, a.phone, u.username " +
                     "FROM admins a " +
                     "LEFT JOIN users u ON a.user_id = u.id " +
                     "WHERE a.id = ?";

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Admin admin = new Admin();
                    admin.setId(rs.getInt("id"));
                    admin.setName(rs.getString("name"));
                    admin.setEmail(rs.getString("email"));
                    admin.setPhone(rs.getString("phone"));
                    admin.setUsername(rs.getString("username"));
                    return admin;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // not found
    }

    // Get all admins
    public List<Admin> getAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT a.id, a.name, a.email, a.phone, u.username "
                + "FROM admins a LEFT JOIN users u ON a.user_id = u.id ORDER BY a.id DESC";

        try (Connection conn = DBConnectionManager.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Admin admin = new Admin();
                admin.setId(rs.getInt("id"));
                admin.setName(rs.getString("name"));
                admin.setEmail(rs.getString("email"));
                admin.setPhone(rs.getString("phone"));
                admin.setUsername(rs.getString("username"));
                admins.add(admin);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return admins;
    }

    // Delete admin + user
    public boolean deleteAdmin(int adminId) {
        String selectUserId = "SELECT user_id FROM admins WHERE id=?";
        String deleteAdminSQL = "DELETE FROM admins WHERE id=?";
        String deleteUserSQL = "DELETE FROM users WHERE id=?";

        try (Connection conn = DBConnectionManager.getConnection()) {
            conn.setAutoCommit(false);

            int userId;
            try (PreparedStatement stmt = conn.prepareStatement(selectUserId)) {
                stmt.setInt(1, adminId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("user_id");
                    } else {
                        throw new SQLException("Admin not found.");
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(deleteAdminSQL)) {
                stmt.setInt(1, adminId);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(deleteUserSQL)) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update admin profile (name/email/phone)
    public boolean updateAdmin(Admin admin) {
        String sql = "UPDATE admins SET name=?, email=?, phone=? WHERE id=?";
        try (Connection conn = DBConnectionManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, admin.getName());
            stmt.setString(2, admin.getEmail());
            stmt.setString(3, admin.getPhone());
            stmt.setInt(4, admin.getId());

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update username and optional password
    public boolean updateAdminCredentials(Admin admin, String username, String password) {
        if (username == null || username.isEmpty()) {
            return false;
        }

        String sql;
        if (password != null && !password.isEmpty()) {
            sql = "UPDATE users SET username=?, password=? WHERE id=(SELECT user_id FROM admins WHERE id=?)";
        } else {
            sql = "UPDATE users SET username=? WHERE id=(SELECT user_id FROM admins WHERE id=?)";
        }

        try (Connection conn = DBConnectionManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            if (password != null && !password.isEmpty()) {
                stmt.setString(2, password);
                stmt.setInt(3, admin.getId());
            } else {
                stmt.setInt(2, admin.getId());
            }

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
